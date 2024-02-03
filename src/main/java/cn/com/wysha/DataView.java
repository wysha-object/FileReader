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
    private JPanel contentPane;
    private JScrollPane jScrollPane;
    private JFrame jFrame;
    private int radix;

    public DataView(int v, int radix) {
        this.radix = radix;
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jFileChooser.setMultiSelectionEnabled(true);
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
                byte[] bytes = new byte[v];
                int index = 0;
                while (inputStream.read(bytes) != -1) {
                    String[] os = new String[size];
                    os[0] = Integer.toString(index, 10);
                    for (int i = 1; i < size; i += 2) {
                        int data = bytes[(i - 1) / 2];
                        if (data < 0) {
                            data += 256;
                        }
                        os[i] = Integer.toString(data, radix);
                        os[i + 1] = String.valueOf((char) data);
                    }
                    linkedList.add(os);
                    ++index;
                }
                dataArray = linkedList.toArray(new String[0][0]);
                table = new JTable();
                table.getTableHeader().setReorderingAllowed(false);
                contentPane = new JPanel(new BorderLayout());
                contentPane.add(table.getTableHeader(), BorderLayout.NORTH);
                jFrame = new JFrame(current.getAbsolutePath());
                jScrollPane = new JScrollPane(table);
                contentPane.add(jScrollPane, BorderLayout.CENTER);
                jFrame.setContentPane(jScrollPane);
                Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize().getSize();
                jFrame.setSize(dimension.width / 2, dimension.height / 2);
                jFrame.setVisible(true);
                DefaultTableModel defaultTableModel = getDefaultTableModel();
                table.setModel(defaultTableModel);
                jFrame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        Choose choose = new Choose();
                        choose.setVisible(true);
                        try {
                            if (choose.isChoose()) {
                                save();
                            }
                        } catch (Exception exception) {
                            System.exit(exception.hashCode());
                        }
                    }
                });
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void save() throws Exception{
        OutputStream outputStream=new FileOutputStream(current);
        for (String[] ss:dataArray){
            for (int i = 1; i < ss.length; i+=2) {
                outputStream.write(new byte[]{(byte) Integer.parseInt(ss[i],radix)});
            }
        }
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
