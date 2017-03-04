import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by angela on 17.02.17.
 */
public class StoredFilesCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
    private JButton btn1, btn2, btn3;
    private GUI gui;
    private JPanel panel;
    private FileEntry file;
    private static final String EDIT = "edit";

    public StoredFilesCellEditor(GUI gui) {
        this.gui = gui;
        StoredFilesCellRenderer renderer = new StoredFilesCellRenderer();
        panel = renderer.create();
        btn1 = renderer.getBtn(1);
        btn2 = renderer.getBtn(2);
        btn3 = renderer.getBtn(3);
        btn1.addActionListener(e -> launchEditor());
        btn2.addActionListener(e -> downloadFromServer());
        btn3.addActionListener(e -> deleteFileFromDisk());
    }

    public void launchEditor() {
        new FileEditor(new File(Main.DATA_DIR+File.separator+file.fileName), gui);
    }

    public void downloadFromServer() {
        try {
            NetCommunication.getFromServer(file.fileName);
        }
        catch (FileNotFoundException e) {
            gui.showErrorMessage("File not found");
        }
        catch (IOException e) {
            gui.showErrorMessage();
        }
        gui.updateStoredFilesTable();
    }

    public void deleteFileFromDisk() {
        Pattern regex = Pattern.compile("(\\S*) (\\d*) (.*)");
        try {
            new File(Main.DATA_DIR+File.separator+file.fileName).delete();
            BufferedReader datafileReader = new BufferedReader(new FileReader(Main.DATA_PATH));
            BufferedWriter datafileWriter = new BufferedWriter(new FileWriter(Main.DATA_PATH + ".tmp"));
            datafileReader.lines().forEach(s -> {
                Matcher m = regex.matcher(s);
                m.find();
                String f = m.group(3);
                try {
                    if (!f.equals(file.fileName))
                        datafileWriter.write(s+"\n");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            datafileReader.close();
            datafileWriter.close();
            File datafile = new File(Main.DATA_PATH);
            File tmpdatafile = new File(Main.DATA_PATH + ".tmp");
            datafile.delete();
            tmpdatafile.renameTo(datafile);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        gui.updateStoredFilesTable();
    }

    public void actionPerformed(ActionEvent e) {
        if (EDIT.equals(e.getActionCommand())) {
            fireEditingStopped();
        }
    }

    public Object getCellEditorValue() {
        return file;
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        file = (FileEntry)value;
        return panel;
    }
}
