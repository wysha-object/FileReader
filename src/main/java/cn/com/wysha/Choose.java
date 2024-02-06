package cn.com.wysha;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Choose extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JSpinner start;
    private JSpinner end;
    public long[] getValue(){
        return new long[]{(int) start.getValue(), (int) end.getValue()};
    }
    public Choose() {
        setTitle("Choose");
        setContentPane(contentPane);
        setModal(true);

        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize().getSize();
        setSize(dimension.width / 4, dimension.height / 4);
        setLocationRelativeTo(null);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    private void onOK() {
        dispose();
    }
}
