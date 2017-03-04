import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBException;
import java.io.*;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Created by angela on 17.02.17.
 */
class ClientCommunication extends Thread {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private XMLChecker checker;
    private ConcurrentHashMap<String, Main.FileEntry> map;
    private Locker locker;

    ClientCommunication(Socket socket, XMLChecker checker, ConcurrentHashMap<String, Main.FileEntry> map,
                        Locker locker)  throws IOException {
        this.socket = socket;
        this.checker = checker;
        this.map = map;
        this.locker = locker;
        System.out.println("[INFO] client connected");
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    @Override
    public void run() {
        try {
            String cmd = reader.readLine();
            System.out.println("[INFO] cmd: " + cmd);
            Matcher matcher1 = Pattern.compile("GET (.*)").matcher(cmd);
            Matcher matcher2 = Pattern.compile("GETSHA (\\S*)").matcher(cmd);
            Matcher matcher3 = Pattern.compile("LIST").matcher(cmd);
            Matcher matcher4 = Pattern.compile("SEND (.*)").matcher(cmd);
            Matcher matcher5 = Pattern.compile("SYNC").matcher(cmd);
            if (matcher1.matches()) {
                locker.lockReader();
                try {
                    get(matcher1);
                } finally { //no matter what happens, we must unlock reader/writer!
                    locker.unlockReader();
                }
            }
            else if(matcher2.matches()) {
                locker.lockReader();
                try {
                    getsha(matcher2);
                } finally {
                    locker.unlockReader();
                }
            }
            else if (matcher3.matches()) {
                locker.lockReader();
                try {
                    list();
                } finally {
                    locker.unlockReader();
                }
            }
            else if (matcher4.matches()) {
                locker.lockWriter();
                try {
                    send(matcher4);
                } finally {
                    locker.unlockWriter();
                }
            }
            else if (matcher5.matches()) {
                locker.lockReader();
                try {
                    sync();
                } finally {
                    locker.unlockReader();
                }
            }
            else
                writer.write("BAD COMMAND\n");
            writer.flush();
            System.out.println("[INFO] the end");
            socket.close();
        }
        catch (IOException e) {
            System.out.println("[ERROR] client: "+socket.toString());
            System.out.println(e.getMessage());
        }
    }

    private void get(Matcher m) throws IOException {
        try {
            String filename = m.group(1);
            System.out.println("[INFO] client wants to get file " + Main.DATA_DIR + File.separator + filename);
            if(!map.containsKey(filename))
                throw new FileNotFoundException();
            Main.FileEntry data = map.get(filename);
            File file = new File(Main.DATA_DIR + File.separator + data.sha);
            writer.write("OK\n");
            writer.write(data.sha + " " + data.date + " " + data.filename + "\n");
            writeFileToSocket(file);
        } catch (FileNotFoundException e) {
            System.out.println("[INFO] not found");
            writer.write("FILE NOT FOUND\n");
        } catch (Exception e) {
            e.printStackTrace();
            writer.write("ERROR\n");
        }
    }

    private void getsha(Matcher m) throws IOException {
        try {
            String sha = m.group(1);
            System.out.println("[INFO] client wants to get file with sha " + sha);
            File file = new File(Main.DATA_DIR + File.separator + sha);
            if(!file.exists())
                throw new FileNotFoundException();
            writer.write("OK\n");
            writeFileToSocket(file);
        } catch (FileNotFoundException e) {
            System.out.println("[INFO] not found");
            writer.write("FILE NOT FOUND\n");
        } catch (Exception e) {
            e.printStackTrace();
            writer.write("ERROR\n");
        }
    }

    private void list() throws IOException {
        File f = new File(Main.DATA_DIR + File.separator + Main.DATA_FILE);
        BufferedReader fileReader = new BufferedReader(new FileReader(f));
        Stream<String> stringStream = fileReader.lines();
        ArrayList<String> lines = new ArrayList<>();
        stringStream.forEach(lines::add);
        writer.write(lines.size() + "\n");
        for (String s : lines)
            writer.write(s + "\n");
        fileReader.close();
    }

    private void send(Matcher matcher) throws IOException {
        try {
            String filename = matcher.group(1);
            System.out.println("[INFO] client wants to send file " + Main.DATA_DIR + File.separator + filename);
            String dt = readFileFromSocket(filename);
            writer.write("OK\n");
            writer.write(dt+"\n");
        } catch (JAXBException e) {
            writer.write("XML ERROR\n");
            writer.write(e.getMessage() == null ? "" : e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            writer.write("ERROR\n");
        }
    }

    private void sync() throws IOException {
        int count = Integer.parseInt(reader.readLine());
        HashMap<String, Main.FileEntry> map2 = new HashMap<>(map);
        for (int i = 0; i < count; ++i) {
            String line = reader.readLine();
            Matcher m = Main.regex.matcher(line);
            m.find();
            long date = Long.parseLong(m.group(2));
            String filename = m.group(3);
            if(date == map2.get(filename).date)
                map2.remove(filename);
        }
        writer.write("OK\n");
        writer.write(map2.size()+"\n");
        for(Main.FileEntry fileEntry : map2.values()) {
            writer.write(fileEntry.sha+" "+fileEntry.date+" "+fileEntry.filename+"\n");
            writeFileToSocket(new File(Main.DATA_DIR+File.separator+fileEntry.sha));
        }
        list();
    }

    private String readFileFromSocket(String filename) throws IOException, NoSuchAlgorithmException, JAXBException {
        Date now = new Date();
        File dst = new File(Main.DATA_DIR+File.separator+".tmp");
        long fileLength = Integer.parseInt(reader.readLine());
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        char bytes[] = new char[4096];
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(dst));
        int ct;
        long read = 0;
        while (read < fileLength && (ct = reader.read(bytes, 0, (int)Math.min(fileLength-read, 4096))) > 0) {
            read += ct;
            fileWriter.write(bytes, 0, ct);
            messageDigest.update(new String(bytes, 0, ct).getBytes());
        }
        fileWriter.close();
        String err = checker.check(dst.getCanonicalPath());
        if(!err.equals(""))
            throw new JAXBException(err);
        String shaSum = DatatypeConverter.printHexBinary(messageDigest.digest());
        String dt = shaSum + " " + now.getTime() + " " + filename;

        FileWriter fw = new FileWriter(Main.DATA_PATH, true);
        fw.write(dt+"\n");
        fw.close();
        map.put(filename, new Main.FileEntry(shaSum, now.getTime(), filename));
        File sdst = new File(Main.DATA_DIR+File.separator+shaSum);
        if(sdst.exists()) return dt;
        dst.renameTo(sdst);
        return dt;
    }

    private void writeFileToSocket(File src) throws IOException {
        char bytes[] = new char[4096];
        BufferedReader fileInputStream = new BufferedReader(new FileReader(src));
        long filelen = 0;
        int ct;
        while ((ct = fileInputStream.read(bytes)) > 0)
            filelen += ct;
        fileInputStream.close();
        writer.write(filelen+"\n");
        fileInputStream = new BufferedReader(new FileReader(src));
        while ((ct = fileInputStream.read(bytes)) > 0)
            writer.write(bytes, 0, ct);
        fileInputStream.close();
    }
}
