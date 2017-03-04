import java.util.Date;

/**
 * Created by angela on 17.02.17.
 */
public class FileEntry {
    public FileEntry(String shaSum, Date modificationDate, String fileName) {
        this.fileName = fileName;
        this.modificationDate = modificationDate;
        this.shaSum = shaSum;
    }
    String fileName;
    Date modificationDate;
    String shaSum;
}
