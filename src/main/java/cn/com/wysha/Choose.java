package cn.com.wysha;

import javax.swing.*;
import java.awt.*;

public class Choose extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JSpinner start;
    private JSpinner end;
    private JButton cancelButton;
    private boolean choose;

    public boolean isChoose() {
        return choose;
    }

    public long[] getValue(){
        return new long[]{(int) start.getValue(), (int) end.getValue()};
    }
    public Choose() {
        this.choose=false;
        setTitle("Choose");
        setContentPane(contentPane);
        setModal(true);

        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize().getSize();
        setSize(dimension.width / 4, dimension.height / 4);
        setLocationRelativeTo(null);


        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        buttonOK.addActionListener(e -> onOK());
        cancelButton.addActionListener(e -> onCancel());
    }
    private void onOK(){
            this.choose = true;
            dispose();
    }
    private void onCancel(){
            dispose();
    }
}
