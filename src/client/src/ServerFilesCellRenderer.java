import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * Created by angela on 18.02.17.
 */
public class ServerFilesCellRenderer extends JLabel implements TableCellRenderer {
    private JPanel panel;
    private JButton btn1;
    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value,
            boolean isSelected, boolean hasFocus,
            int row, int column) {
        return create(value);
    }

    public JPanel create(Object value) {
        panel = new JPanel();
        panel.setOpaque(false);
        if(value == null) return panel;
        ImageIcon icon = new ImageIcon(getClass().getResource("icons/download.png"),
                "download");
        btn1 = new JButton(icon);
        Dimension d = new Dimension(16, 16);
        btn1.setMaximumSize(d);
        btn1.setBackground(Color.WHITE);
        btn1.setToolTipText("Download this version from the server");
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.add(btn1);
        return panel;
    }

    public JButton getBtn() {
        return btn1;
    }
}
