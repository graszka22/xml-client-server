import java.io.File;
import java.io.IOException;

/**
 * Created by angela on 14.02.17.
 */
public class Main {
    public static final String DATA_DIR = "data_client";
    public static final String DATA_FILE = ".data";
    public static final String DATA_PATH = DATA_DIR+ File.separator+DATA_FILE;

    public static void main(String[] args) {
        try {
            new File(DATA_DIR).mkdir();
            new File(DATA_PATH).createNewFile();
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
        javax.swing.SwingUtilities.invokeLater(() -> {
            GUI gui = new GUI();
        });
    }
}
