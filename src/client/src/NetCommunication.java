import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Created by angela on 15.02.17.
 */
public class NetCommunication {
    private static String HOST = "localhost";
    private static int PORT = 6666;
    private static Pattern regex = Pattern.compile("(\\S*) (\\d*) (.*)");

    public static void setAddr(String host, int port) {
        HOST = host;
        PORT = port;
    }

    public static void getFromServer(String filename) throws IOException {
        Socket socket = new Socket(HOST, PORT);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out.println("GET " + filename + "\n");
        out.flush();
        String status = reader.readLine();
        if (status.equals("FILE NOT FOUND"))
            throw new FileNotFoundException();
        if (!status.equals("OK"))
            throw new IOException();
        String newFileDesc = reader.readLine();
        readFileFromSocket(reader, filename);
        updateData(filename, newFileDesc);
        socket.close();
    }

    public static void getFromServer(String filename, FileEntry fileEntry) throws IOException {
        Socket socket = new Socket(HOST, PORT);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out.println("GETSHA " + fileEntry.shaSum + "\n");
        out.flush();
        String status = reader.readLine();
        if (status.equals("FILE NOT FOUND"))
            throw new FileNotFoundException();
        if (!status.equals("OK"))
            throw new IOException();
        readFileFromSocket(reader, filename);
        updateData(filename, fileEntry.shaSum + " " +
                fileEntry.modificationDate.getTime() + " " + fileEntry.fileName);
        socket.close();
    }

    public static ArrayList<FileEntry> getListOfFiles() throws IOException {
        ArrayList<FileEntry> files = new ArrayList<>();
        Socket socket = new Socket(HOST, PORT);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out.println("LIST");
        out.flush();
        long count = Integer.parseInt(reader.readLine());
        for (long i = 0; i < count; ++i) {
            Matcher m = regex.matcher(reader.readLine());
            m.find();
            FileEntry entry = new FileEntry(m.group(1),
                    new Date(Long.parseLong(m.group(2))), m.group(3));
            files.add(entry);
        }
        socket.close();
        return files;
    }

    public static ArrayList<FileEntry> synchronize() throws IOException {
        Socket socket = new Socket(HOST, PORT);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer.write("SYNC\n");
        HashMap<String, String> map = new HashMap<>();
        ArrayList<String> lines = new ArrayList<>();
        BufferedReader dataFileReader = new BufferedReader(new FileReader(Main.DATA_PATH));
        String line;
        while ((line = dataFileReader.readLine()) != null)
            lines.add(line);
        writer.write(lines.size() + "\n");
        for (String s : lines) {
            writer.write(s + "\n");
            Matcher m = regex.matcher(s);
            m.find();
            map.put(m.group(), s);
        }
        dataFileReader.close();
        writer.flush();
        if (!reader.readLine().equals("OK"))
            throw new IOException();
        int filesCount = Integer.parseInt(reader.readLine());
        for (int i = 0; i < filesCount; ++i) {
            line = reader.readLine();
            Matcher m = regex.matcher(line);
            m.find();
            String filename = m.group(3);
            map.put(filename, line);
            readFileFromSocket(reader, filename);
        }
        BufferedWriter dataFileWriter = new BufferedWriter(new FileWriter(Main.DATA_PATH));
        for (String s : map.values())
            dataFileWriter.write(s + "\n");
        dataFileWriter.close();
        long count = Integer.parseInt(reader.readLine());
        ArrayList<FileEntry> files = new ArrayList<>();
        for (long i = 0; i < count; ++i) {
            Matcher m = regex.matcher(reader.readLine());
            m.find();
            FileEntry entry = new FileEntry(m.group(1),
                    new Date(Long.parseLong(m.group(2))), m.group(3));
            files.add(entry);
        }
        socket.close();
        return files;
    }

    public static void sendToServer(String filename, String fileToSend) throws IOException {
        Socket socket = new Socket(HOST, PORT);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer.write("SEND " + filename + "\n");
        writeFileToSocket(writer, fileToSend);
        writer.flush();
        String status = reader.readLine();
        if(status.equals("XML ERROR"))
            throw new IllegalStateException(reader.readLine());
        if (!status.equals("OK"))
            throw new IOException();
        String newFileDesc = reader.readLine();
        updateData(filename, newFileDesc);
        socket.close();
    }

    private static void writeFileToSocket(BufferedWriter writer, String filename) throws IOException {
        File file = new File(Main.DATA_DIR + File.separator + filename);
        BufferedReader fileReader = new BufferedReader(new FileReader(file));
        char buf[] = new char[4096];
        int ct;
        //I could use file.length() but it gives me size in bytes, not chars count
        long filelen = 0;
        while ((ct = fileReader.read(buf)) > 0)
            filelen += ct;
        writer.write(filelen + "\n");
        fileReader.close();
        fileReader = new BufferedReader(new FileReader(file));
        while ((ct = fileReader.read(buf)) > 0)
            writer.write(buf, 0, ct);
        fileReader.close();
    }

    private static void readFileFromSocket(BufferedReader reader, String filename) throws IOException {
        long fileSize = Long.parseLong(reader.readLine());
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(Main.DATA_DIR + File.separator + filename)));
        char buf[] = new char[4096];
        int ct;
        long read = 0;
        while (read < fileSize && (ct = reader.read(buf, 0, (int) Math.min(fileSize - read, 4096))) > 0) {
            writer.write(buf, 0, ct);
            read += ct;
        }
        writer.close();
    }

    private static void updateData(String filename, String newFileDesc) throws IOException {
        BufferedReader datafileReader = new BufferedReader(new FileReader(Main.DATA_PATH));
        BufferedWriter datafileWriter = new BufferedWriter(new FileWriter(Main.DATA_PATH + ".tmp"));
        String line;
        boolean added = false;
        while ((line = datafileReader.readLine()) != null) {
            Matcher m = regex.matcher(line);
            m.find();
            String f = m.group(3);
            if (f.equals(filename)) {
                datafileWriter.write(newFileDesc + "\n");
                added = true;
            } else {
                datafileWriter.write(line + "\n");
            }
        }
        if (!added)
            datafileWriter.write(newFileDesc + "\n");
        datafileReader.close();
        datafileWriter.close();
        File datafile = new File(Main.DATA_PATH);
        File tmpdatafile = new File(Main.DATA_PATH + ".tmp");
        datafile.delete();
        tmpdatafile.renameTo(datafile);
    }
}
