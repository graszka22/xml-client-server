import javax.swing.table.AbstractTableModel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by angela on 17.02.17.
 */
public class StoredFilesTableModel extends AbstractTableModel {
    private String[] columnNames = {"Filename", "Date", "SHA", "Actions"};
    private Object[][] data;

    StoredFilesTableModel() throws FileNotFoundException {
        update();
    }

    public void update() throws FileNotFoundException {
        BufferedReader br = new BufferedReader(new FileReader(Main.DATA_PATH));
        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Pattern regex = Pattern.compile("(\\S*) (\\d*) (.*)");
        ArrayList<String> lines = new ArrayList<>();
        br.lines().forEach(lines::add);
        data = new Object[lines.size()][];
        for (int i = 0; i < lines.size(); ++i) {
            Matcher m = regex.matcher(lines.get(i));
            m.find();
            FileEntry entry = new FileEntry(m.group(1),
                    new Date(Long.parseLong(m.group(2))), m.group(3));
            data[i] = new Object[]{entry.fileName, dt.format(entry.modificationDate), entry.shaSum, entry};
        }
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return data.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        return data[row][col];
    }

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    public boolean isCellEditable(int row, int col) {
        return col == 3;
    }

    public void setValueAt(Object value, int row, int col) {
        data[row][col] = value;
        fireTableCellUpdated(row, col);
    }
}
