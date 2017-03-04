import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Created by angela on 17.02.17.
 */
class StoredFilesCellRenderer extends JLabel implements TableCellRenderer {
    private JPanel panel;
    private JButton btn1, btn2, btn3;

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value,
            boolean isSelected, boolean hasFocus,
            int row, int column) {
        return create();
    }

    public JPanel create() {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setOpaque(false);
        ImageIcon icon1 = new ImageIcon(getClass().getResource("icons/new-file.png"),
                "edit");
        ImageIcon icon2 = new ImageIcon(getClass().getResource("icons/download.png"),
                "download");
        ImageIcon icon3 = new ImageIcon(getClass().getResource("icons/multiply.png"),
                "delete");
        btn1 = new JButton(icon1);
        btn2 = new JButton(icon2);
        btn3 = new JButton(icon3);
        btn1.setToolTipText("Edit this file");
        btn2.setToolTipText("Download the newest version of this file");
        btn3.setToolTipText("Delete this file from disk");
        Dimension d = new Dimension(16, 16);
        btn1.setMaximumSize(d);
        btn1.setBackground(Color.WHITE);
        btn2.setMaximumSize(d);
        btn2.setBackground(Color.WHITE);
        btn3.setMaximumSize(d);
        btn3.setBackground(Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.add(btn1);
        panel.add(Box.createRigidArea(new Dimension(5,0)));
        panel.add(btn2);
        panel.add(Box.createRigidArea(new Dimension(5,0)));
        panel.add(btn3);
        return panel;
    }

    public JButton getBtn(int btn) {
        if(btn == 1) return btn1;
        if(btn == 2) return btn2;
        if(btn == 3) return btn3;
        return null;
    }
}
