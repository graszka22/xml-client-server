import org.jdesktop.swingx.JXTreeTable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Created by angela on 17.02.17.
 */
public class GUI {
    private JFrame frame;
    private JXTreeTable serverFilesTreeTable;
    private JTable storedFilesTable;
    private StoredFilesTableModel storedFilesTableModel;

    public GUI() {
        frame = new JFrame("XML client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setLayout(new GridBagLayout());
        JTextField hostField = new JTextField("localhost");
        JSpinner portField = new JSpinner(new SpinnerNumberModel(6666, 1, 65536, 1));
        portField.setEditor(new JSpinner.NumberEditor(portField, "#"));
        JButton ok = new JButton("OK");
        ok.addActionListener(e -> {
            NetCommunication.setAddr(hostField.getText(), (Integer) portField.getValue());
            frame.dispose();
            runMainApp();
        });
        GridBagConstraints c = new GridBagConstraints();
        c.ipadx = 10;
        c.ipady = 10;
        c.insets = new Insets(10, 10, 10, 10);
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        panel.add(new Label("Host:"), c);
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(hostField, c);
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        panel.add(new Label("Port:"), c);
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(portField, c);
        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        c.weightx = 0;
        panel.add(ok, c);
        frame.getContentPane().add(panel);
        frame.setPreferredSize(new Dimension(300, 300));

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void runMainApp() {
        frame = new JFrame("XML client");
        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new BorderLayout(10, 10));
        outerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel upperPanel = new JPanel();
        upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.LINE_AXIS));

        JButton newFileBtn = new JButton("NEW FILE");
        newFileBtn.addActionListener(e -> newFile());
        upperPanel.add(newFileBtn);
        newFileBtn.setToolTipText("Create a new file");
        JButton syncBtn = new JButton("SYNC");
        syncBtn.addActionListener(e -> sync());
        upperPanel.add(syncBtn);
        syncBtn.setToolTipText("Download newest version of files from server");
        JButton refreshBtn = new JButton("REFRESH");
        refreshBtn.addActionListener(e -> refreshServerFiles());
        refreshBtn.setToolTipText("Get list of files on the server");
        upperPanel.add(refreshBtn);

        try {
            storedFilesTableModel = new StoredFilesTableModel();
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(frame,
                    "Can't access data file.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        storedFilesTable = new JTable(storedFilesTableModel);
        storedFilesTable.setFillsViewportHeight(true);
        storedFilesTable.setRowHeight(25);
        storedFilesTable.getColumn("Actions").setCellEditor(new StoredFilesCellEditor(this));
        storedFilesTable.getColumn("Actions").setCellRenderer(new StoredFilesCellRenderer());

        ServerFilesTreeTableModel serverFilesTreeTableModel = new ServerFilesTreeTableModel(new ArrayList<>());
        serverFilesTreeTable = new JXTreeTable(serverFilesTreeTableModel);
        serverFilesTreeTable.packAll();

        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(storedFilesTable),
                new JScrollPane(serverFilesTreeTable));
        mainSplitPane.setDividerLocation(0.5);
        mainSplitPane.setResizeWeight(0.5);
        outerPanel.add(upperPanel, BorderLayout.NORTH);
        outerPanel.add(mainSplitPane, BorderLayout.CENTER);

        frame.setPreferredSize(new Dimension(700, 500));
        frame.getContentPane().add(outerPanel);

        frame.pack();
        frame.setVisible(true);
        refreshServerFiles();
    }

    private void newFile() {
        String s = JOptionPane.showInputDialog(frame, "New file name:");
        if (s == null || s.length() == 0)
            return;
        if (!s.endsWith(".xml")) {
            JOptionPane.showMessageDialog(frame,
                    "Filename should end with \".xml\".",
                    "Bad filename",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        File file = new File(Main.DATA_DIR + File.separator + s);
        if (file.exists()) {
            JOptionPane.showMessageDialog(frame,
                    "File already exists.",
                    "Error creating file",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        new FileEditor(file, this);
        updateStoredFilesTable();
    }

    private void sync() {
        ArrayList<FileEntry> files = null;
        try {
            files = NetCommunication.synchronize();
        } catch (IOException e) {
            showErrorMessage();
        }
        updateStoredFilesTable();
        updateServerFilesTable(files);
    }

    public void refreshServerFiles() {
        ArrayList<FileEntry> files = null;
        try {
            files = NetCommunication.getListOfFiles();
        } catch (IOException e) {
            showErrorMessage();
            return;
        }
        updateServerFilesTable(files);
    }

    public void updateStoredFilesTable() {
        try {
            storedFilesTableModel = new StoredFilesTableModel();
            storedFilesTable.setModel(storedFilesTableModel);
            storedFilesTable.getColumn("Actions").setCellEditor(new StoredFilesCellEditor(this));
            storedFilesTable.getColumn("Actions").setCellRenderer(new StoredFilesCellRenderer());
        } catch (FileNotFoundException e) {
            showErrorMessage("Can't access data file.");
        }
    }

    public void updateServerFilesTable(ArrayList<FileEntry> files) {
        HashMap<String, ServerFilesTreeTableModel.FileList> map = new HashMap<>();
        for (FileEntry f : files) {
            if (!map.containsKey(f.fileName))
                map.put(f.fileName, new ServerFilesTreeTableModel.FileList(f.fileName));
            map.get(f.fileName).addFileEntry(f);
        }
        List<ServerFilesTreeTableModel.FileList> fileLists = new ArrayList<>();
        for (ServerFilesTreeTableModel.FileList f : map.values())
            fileLists.add(f);
        serverFilesTreeTable.setTreeTableModel(new ServerFilesTreeTableModel(fileLists));
        serverFilesTreeTable.getColumnModel().getColumn(3).setCellRenderer(new ServerFilesCellRenderer());
        serverFilesTreeTable.getColumnModel().getColumn(3).setCellEditor(new ServerFilesCellEditor(this));
    }

    public void showErrorMessage() {
        JOptionPane.showMessageDialog(frame,
                "Something bad happened. Problem with server connection or file i/o.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    public void showErrorMessage(String msg) {
        JOptionPane.showMessageDialog(frame,
                msg,
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
