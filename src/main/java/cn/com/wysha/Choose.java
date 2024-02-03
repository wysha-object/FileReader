package cn.com.wysha;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Choose extends JDialog {
    private JPanel contentPane;
    private JButton buttonYes;
    private JButton buttonNo;
    private boolean choose;
    public boolean isChoose() {
        return choose;
    }
    public Choose() {
        setTitle("Choose");
        setContentPane(contentPane);
        setModal(true);

        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize().getSize();
        setSize(dimension.width / 3, dimension.height / 3);
        setLocationRelativeTo(null);

        buttonYes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonNo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });
    }

    private void onOK() {
        choose=true;
        dispose();
    }

    private void onCancel() {
        choose=false;
        dispose();
    }
}
