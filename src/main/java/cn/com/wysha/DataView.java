package cn.com.wysha;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.LinkedList;

/**
 * @author wysha
 */
public class DataView {
    private File current;
    private String[][] dataArray;
    private String[] objects;
    private JTable table;
    private final int radix;
    public DataView(int v, int radix) {
        this.radix = radix;
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jFileChooser.setMultiSelectionEnabled(true);
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize().getSize();
        if (
                jFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION
        ) {
            current = jFileChooser.getSelectedFile();
            int size = v * 2 + 1;
            objects = new String[size];
            objects[0] = "index";
            for (int i = 1; i < size; i += 2) {
                int index = i / 2;
                String string = Integer.toString(index, 10);
                objects[i] = string + "-value";
                objects[i + 1] = string + "-char";
            }
            LinkedList<String[]> linkedList = new LinkedList<>();
            try (InputStream inputStream = new FileInputStream(current)) {
                int index = 0;
                int i = 1;
                byte[] bytes = new byte[1];
                String[] os = new String[size];
                os[0] = Integer.toString(index, 10);
                System.out.print('\n');
                while (inputStream.read(bytes) != -1) {
                    int n = index * v + (i + 1) / 2;
                    int a = (int) (((double) n) / current.length() * 100);
                    a = Math.min(a, 100);
                    System.out.print(current.getAbsolutePath() + "\tread progress:\t" + "\t|\t" + "|".repeat(a) + " ".repeat(100 - a) + "\t|\t" + n + '/' + current.length() + "(byte)\r");
                    int data = bytes[0];
                    if (data < 0) {
                        data += 256;
                    }
                    os[i] = Integer.toString(data, radix);
                    os[i + 1] = String.valueOf((char) data);
                    i += 2;
                    if (i >= size) {
                        ++index;
                        i = 1;
                        linkedList.add(os);
                        os = new String[size];
                        os[0] = Integer.toString(index, 10);
                    }
                }
                System.out.print('\n');
                if (i != 1) {
                    linkedList.add(os);
                }
                dataArray = linkedList.toArray(new String[0][0]);
                table = new JTable();
                table.getTableHeader().setReorderingAllowed(false);
                JPanel contentPane = new JPanel(new BorderLayout());
                JFrame jFrame = new JFrame(current.getAbsolutePath());
                JPanel panel = new JPanel(new BorderLayout());
                panel.add(table.getTableHeader(), BorderLayout.NORTH);
                panel.add(new JScrollPane(table), BorderLayout.CENTER);
                contentPane.add(panel, BorderLayout.CENTER);
                JButton jButton = getjButton();
                contentPane.add(jButton, BorderLayout.SOUTH);
                jFrame.setContentPane(contentPane);
                jFrame.setSize(dimension.width / 2, dimension.height / 2);
                DefaultTableModel defaultTableModel = getDefaultTableModel();
                table.setModel(defaultTableModel);
                jFrame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        Choose choose = new Choose();
                        choose.setVisible(true);
                        try {
                            if (choose.isChoose()) {
                                save(null);
                            }
                        } catch (Exception exception) {
                            exception.printStackTrace();
                            System.exit(exception.hashCode());
                        }
                    }
                });
                jFrame.setVisible(true);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private JButton getjButton() {
        JButton jButton = new JButton("Stitch the specified data to the end and save it.");
        jButton.addActionListener(eve -> {
            JFileChooser jFileChooser = new JFileChooser();
            jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jFileChooser.setMultiSelectionEnabled(true);
            if (jFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File file = jFileChooser.getSelectedFile();
                try (InputStream input = new FileInputStream(file)) {
                    byte[] bs = new byte[(int) file.length()];
                    byte[] by = new byte[1];
                    System.out.print('\n');
                    int n = 0;
                    while (input.read(by) != -1) {
                        bs[n] = by[0];
                        ++n;
                        int a = (int) (((double) n) / file.length() * 100);
                        a = Math.min(a, 100);
                        System.out.print(file.getAbsolutePath() + "\tread progress:\t" + "\t|\t" + "|".repeat(a) + " ".repeat(100 - a) + "\t|\t" + n + '/' + file.length() + "(byte)\r");
                    }
                    System.out.print('\n');
                    save(bs);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(e.hashCode());
                }
            }
        });
        return jButton;
    }

    private void save(byte[] bytes) throws Exception {
        System.out.print('\n');
        OutputStream outputStream=new FileOutputStream(current);
        int index = 0;
        int length = dataArray.length * (dataArray[0].length - 1) / 2;
        for (String[] ss:dataArray){
            for (int i = 1; i < ss.length; i+=2) {
                if (ss[i] == null) {
                    break;
                }
                outputStream.write(new byte[]{(byte) Integer.parseInt(ss[i],radix)});
                int n = index * (ss.length - 1) / 2 + (i + 1) / 2;
                int a = (int) (((double) n) / length * 100);
                a = Math.min(a, 100);
                System.out.print(current.getAbsolutePath() + "\twrite progress:\t" + "\t|\t" + "|".repeat(a) + " ".repeat(100 - a) + "\t|\t" + n + '/' + length + "(byte)\tThere may be deviations from actual progress.\r");
            }
            ++index;
        }
        if (bytes != null) {
            System.out.print('\n');
            for (int i = 1; i <= bytes.length; i++) {
                outputStream.write(new byte[]{bytes[i - 1]});
                int a = (int) (((double) i) / bytes.length * 100);
                a = Math.min(a, 100);
                System.out.print(current.getAbsolutePath() + "\twriteOther progress:\t" + "\t|\t" + "|".repeat(a) + " ".repeat(100 - a) + "\t|\t" + i + '/' + bytes.length + "(byte)\r");
            }
        }
        System.out.print('\n');
    }

    private DefaultTableModel getDefaultTableModel() {
        DefaultTableModel defaultTableModel = new DefaultTableModel(dataArray, objects) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0;
            }
        };
        defaultTableModel.addTableModelListener(event -> {
            if (event.getType() == TableModelEvent.UPDATE) {
                int row = event.getFirstRow();
                int col = event.getColumn();
                if (row != -1 && col != -1) {
                    if (event.getColumn() % 2 == 0) {
                        char c = ((String) table.getValueAt(row, col)).charAt(0);
                        dataArray[row][col] = String.valueOf(c);
                        dataArray[row][col - 1] = Integer.toString(c, radix);
                    } else {
                        dataArray[row][col] = (String) table.getValueAt(row, col);
                        dataArray[row][col + 1] = String.valueOf((char) Integer.parseInt((String) table.getValueAt(row, col), radix));
                    }
                    defaultTableModel.setDataVector(dataArray, objects);
                }
            }
        });
        return defaultTableModel;
    }
}
