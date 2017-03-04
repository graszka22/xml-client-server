import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;

/**
 * Created by angela on 18.02.17.
 */
public class ServerFilesCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
    private JButton btn1;
    private JPanel panel;
    private FileEntry file;
    private GUI gui;
    private static final String EDIT = "edit";

    public ServerFilesCellEditor(GUI gui) {
        this.gui = gui;
    }

    private void downloadFromServer() {
        try {
            NetCommunication.getFromServer(file.fileName, file);
        }
        catch (FileNotFoundException e) {
            gui.showErrorMessage("File not found");
        }
        catch (IOException e) {
            gui.showErrorMessage();
        }
        gui.updateStoredFilesTable();
    }

    public void actionPerformed(ActionEvent e) {
        if (EDIT.equals(e.getActionCommand())) {
            fireEditingStopped();
        }
    }

    public Object getCellEditorValue() {
        return null;
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        file = (FileEntry) value;
        ServerFilesCellRenderer renderer = new ServerFilesCellRenderer();
        panel = renderer.create(file);
        btn1 = renderer.getBtn();
        btn1.addActionListener(e -> downloadFromServer());
        return panel;
    }
}
