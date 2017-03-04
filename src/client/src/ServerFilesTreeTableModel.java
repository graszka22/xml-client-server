/**
 * Created by angela on 18.02.17.
 */
import java.util.ArrayList;
import java.util.List;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

public class ServerFilesTreeTableModel extends AbstractTreeTableModel {
    public static class FileList {
        private List<FileEntry> fileList = new ArrayList<>();
        private String filename;
        public FileList(String filename) {
            this.filename = filename;
        }

        public List<FileEntry> getFileList() {
            return fileList;
        }

        public void addFileEntry(FileEntry e) {
            fileList.add(e);
        }

        public String getFilename() {
            return filename;
        }
    }
    private final static String[] COLUMN_NAMES = {"Name", "Date", "SHA", "Actions"};

    private List<FileList> fileLists;

    public ServerFilesTreeTableModel(List<FileList> fileLists) {
        super(new Object());
        this.fileLists = fileLists;
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public boolean isCellEditable(Object node, int column) {
        return column == 3 && isLeaf(node);
    }

    @Override
    public boolean isLeaf(Object node) {
        return node instanceof FileEntry;
    }

    @Override
    public int getChildCount(Object parent) {
        if (parent instanceof FileList) {
            FileList dept = (FileList) parent;
            return dept.getFileList().size();
        }
        return fileLists.size();
    }

    @Override
    public Object getChild(Object parent, int index) {
        if (parent instanceof FileList) {
            FileList dept = (FileList) parent;
            return dept.getFileList().get(index);
        }
        return fileLists.get(index);
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        FileList dept = (FileList) parent;
        FileEntry emp = (FileEntry) child;
        return dept.getFileList().indexOf(emp);
    }

    @Override
    public Object getValueAt(Object node, int column) {
        if (node instanceof FileList) {
            FileList dept = (FileList) node;
            switch (column) {
                case 0:
                    return dept.getFilename();
            }
        } else if (node instanceof FileEntry) {
            FileEntry emp = (FileEntry) node;
            switch (column) {
                case 0:
                    return emp.fileName;
                case 1:
                    return emp.modificationDate;
                case 2:
                    return emp.shaSum;
                case 3:
                    return emp;
            }
        }
        return null;
    }

    public List<FileList> getFileLists() {
        return fileLists;
    }
}
