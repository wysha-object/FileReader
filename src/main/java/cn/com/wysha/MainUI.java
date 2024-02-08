package cn.com.wysha;

import javax.swing.*;
import java.awt.*;

import static cn.com.wysha.Config.VERSION;

/**
 * @author wysha
 */
public class MainUI extends JFrame {
    private JPanel contentPane;
    private JButton chooseFileButton;
    private JSpinner numberOfColumns;
    private JLabel columns;
    private JSpinner radixSpinner;
    private JLabel radixLabel;
    private JLabel version;
    private JLabel mail;

    public MainUI() {
        System.out.println("Welcome!");
        setTitle("FileReader "+VERSION);
        version.setText(VERSION);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        numberOfColumns.setValue(16);
        radixSpinner.setValue(16);
        setContentPane(contentPane);
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize().getSize();
        setSize(dimension.width / 2, dimension.height / 2);
        setLocationRelativeTo(null);
        chooseFileButton.addActionListener(e -> {
            JFileChooser jFileChooser = new JFileChooser();
            jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jFileChooser.setMultiSelectionEnabled(true);
            if (jFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                new DataView(jFileChooser.getSelectedFile(),(int) radixSpinner.getValue(), (int) numberOfColumns.getValue()).setVisible(true);
            }
        });
    }
}
