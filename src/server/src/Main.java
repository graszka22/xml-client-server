import org.xml.sax.SAXException;
import schema.*;

import java.io.*;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBException;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

public class Main {
    public static final String DATA_DIR = "data_server";
    public static final String DATA_FILE = ".data";
    public static final String DATA_PATH = DATA_DIR+ File.separator+DATA_FILE;
    public static final Pattern regex = Pattern.compile("(\\S*) (\\d*) (.*)");
    static class FileEntry {
        FileEntry(String sha, long date, String filename) {
            this.sha = sha;
            this.date = date;
            this.filename = filename;
        }
        String filename, sha;
        long date;
    }
    public static void main(String[] args) throws IOException {
        new File(DATA_DIR).mkdir();
        new File(DATA_PATH).createNewFile();
        XMLChecker checker;
        try {
            checker = new XMLChecker();
        } catch (JAXBException | SAXException e) {
            e.printStackTrace();
            return;
        }
        ConcurrentHashMap<String, FileEntry> fileMap = new ConcurrentHashMap<>();
        BufferedReader br = new BufferedReader(new FileReader(Main.DATA_PATH));
        br.lines().forEach(s->{
            Matcher m = regex.matcher(s);
            m.find();
            String sha = m.group(1);
            long date = Long.parseLong(m.group(2));
            String filename = m.group(3);
            fileMap.put(filename, new FileEntry(sha, date, filename));
        });
        Locker locker = new Locker();
        int port = 6666;
        if(args.length > 0)
            port = Integer.parseInt(args[0]);
        ServerSocket serverSocket = new ServerSocket(port);
        while (true) {
            Socket clientSocket = serverSocket.accept();
            //what if client doesn't answer us for a long time? it can block our server!
            clientSocket.setSoTimeout(5000);
            try {
                ClientCommunication communication = new ClientCommunication(clientSocket, checker, fileMap, locker);
                communication.start();
            }
            catch (Exception e) {
                System.out.println("[ERROR] client: "+clientSocket.toString());
                System.out.println(e.getMessage());
            }
        }
    }
}
