import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by angela on 17.02.17.
 */
public class FileEditor {
    private File file;
    private JEditorPane editorPane;
    private JFrame frame;
    private GUI gui;

    public FileEditor(File file, GUI gui) {
        this.file = file;
        this.gui = gui;
        frame = new JFrame(file.getName());
        frame.setSize(new Dimension(700, 500));
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        JButton serverSaveBtn = new JButton("save on disk & server");
        serverSaveBtn.addActionListener(e -> save());
        panel.add(serverSaveBtn, BorderLayout.NORTH);
        try {
            File tmp = new File(Main.DATA_DIR+File.separator+".tmp");
            tmp.createNewFile();
            editorPane = new JEditorPane(tmp.toURI().toURL());
        } catch (IOException e) {
            e.printStackTrace();
        }
        panel.add(new JScrollPane(editorPane), BorderLayout.CENTER);
        frame.getContentPane().add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void save() {
        String txt = editorPane.getText();
        try {
            File tmp = new File(Main.DATA_DIR+File.separator+".tmp");
            FileWriter fw = new FileWriter(tmp);
            fw.write(txt);
            fw.close();
            try {
                NetCommunication.sendToServer(file.getName(), tmp.getName());
                file.delete();
                tmp.renameTo(file);
            }
            catch (IllegalStateException e) {
                gui.showErrorMessage("Bad XML file.\n"+e.getMessage());
                return;
            }
            catch (IOException e) {
                gui.showErrorMessage();
                return;
            }
            frame.dispose();
        } catch (IOException e) {
            gui.showErrorMessage();
            return;
        }
        gui.updateStoredFilesTable();
        gui.refreshServerFiles();
    }
}
