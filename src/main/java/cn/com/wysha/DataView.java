package cn.com.wysha;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;

/**
 * @author wysha
 */
public class DataView extends JFrame {
    private final int radix;
    private final File current;
    private final HashMap<Long, Short> allData = new HashMap<>();
    private final int numberOfColumns;
    private final MyModel valuesJTableModel = new MyModel();
    private final MyModel charsJTableModel = new MyModel();
    private final String[] names;
    private final DefaultTableModel listJTableModel = new DefaultTableModel();
    private long currentStart;
    private long currentEnd;
    private JPanel contentPane;
    private JPanel valuesJPanel;
    private JPanel charsJPanel;
    private JLabel label;
    private JPanel listJPanel;
    private JButton jumpButton;
    private JButton saveButton;
    private JButton readButton;

    public DataView(int radix, int numberOfColumns) {
        setContentPane(contentPane);
        label.setText("radix : " + radix);
        this.numberOfColumns = numberOfColumns;
        this.radix = radix;
        this.names = new String[numberOfColumns];
        for (int i = 0; i < numberOfColumns; i++) {
            names[i] = Integer.toString(i, radix);
        }
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jFileChooser.setMultiSelectionEnabled(true);
        jFileChooser.showOpenDialog(null);
        this.current = jFileChooser.getSelectedFile();
        setTitle(current.getName());
        read(0, numberOfColumns);
        setCurrent(0, numberOfColumns);
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        JTable valuesJTable = new JTable(valuesJTableModel);
        valuesJPanel.add(valuesJTable.getTableHeader(), BorderLayout.NORTH);
        JTable charsJTable = new JTable(charsJTableModel);
        charsJPanel.add(charsJTable.getTableHeader(), BorderLayout.NORTH);
        valuesJPanel.add(new JScrollPane(valuesJTable), BorderLayout.CENTER);
        charsJPanel.add(new JScrollPane(charsJTable), BorderLayout.CENTER);
        valuesJTable.getTableHeader().setReorderingAllowed(false);
        charsJTable.getTableHeader().setReorderingAllowed(false);
        JTable list = new JTable(listJTableModel);
        listJPanel.add(list.getTableHeader(), BorderLayout.NORTH);
        listJPanel.add(new JScrollPane(list), BorderLayout.CENTER);
        list.setEnabled(false);
        setSize(dimension.width / 2, dimension.height / 2);
        readButton.addActionListener(e -> {
            Choose choose=new Choose();
            choose.setVisible(true);
            long[] rs=choose.getValue();
            read(rs[0],rs[1]);
        });
        jumpButton.addActionListener(e -> {
            Choose choose=new Choose();
            choose.setVisible(true);
            long[] rs=choose.getValue();
            setCurrent(rs[0],rs[1]);
        });
        saveButton.addActionListener(e -> {
            Choose choose=new Choose();
            choose.setVisible(true);
            long[] rs=choose.getValue();
            write(rs[0],rs[1]);
        });
    }

    private void setCurrent(long start, long end) {
        currentStart = start;
        currentEnd=end;
        int a=(int) ((end - start) / numberOfColumns);
        int numberOfRows = a + 1;
        String[][] bytes = new String[numberOfRows][numberOfColumns];
        Character[][] chars = new Character[numberOfRows][numberOfColumns];
        String[][] strings = new String[numberOfRows][1];
        int row = -1;
        int col = numberOfColumns;
        strings[a][0] = Long.toString(a, radix);
        for (long i = start; i < end; ++i) {
            System.out.println(current.getName()+"\tsetCurrent\t"+i+"\t/\t"+end);
            if (col == numberOfColumns) {
                ++row;
                col = 0;
                strings[row][0] = Long.toString(row, radix);
            }
            Short b = allData.get(i);
            if (b == null) {
                bytes[row][col] = null;
                chars[row][col] = null;
            } else {
                bytes[row][col] = Integer.toString(b, radix);
                chars[row][col] = (char) (short) b;
            }
            ++col;
        }
        valuesJTableModel.setDataVector(bytes, names);
        charsJTableModel.setDataVector(chars, names);
        listJTableModel.setDataVector(strings, new String[]{"index"});
    }

    private void read(long start, long end) {
        try (FileInputStream fileInputStream = new FileInputStream(current)) {
            fileInputStream.skipNBytes(start);
            long i;
            for (i = start; i < end && i < current.length(); i++) {
                System.out.println(current.getName()+"\tread\t"+i+"\t/\t"+end);
                allData.put(i, (short) fileInputStream.read());
            }
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void write(long start, long end) {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(current, "rwd")) {
            randomAccessFile.seek(start);
            byte[] bytes = new byte[(int) (end - start)];
            for (int i = 0; i < bytes.length; i++) {
                System.out.println(current.getName()+"\twrite\t"+i+"\t/\t"+end);
                Short data=allData.get(start + i);
                if (data==null){
                    continue;
                }
                bytes[i] = (byte)(short)data;
            }
            randomAccessFile.write(bytes);
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    class MyModel extends DefaultTableModel {
        private MyModel() {
            super();
            addTableModelListener(event -> {
                if (event.getType() == TableModelEvent.UPDATE) {
                    int row = event.getFirstRow();
                    int col = event.getColumn();
                    if (row != -1 && col != -1) {
                        allData.put(
                                currentStart + ((long) (row)) * numberOfColumns + col,
                                this==valuesJTableModel? (short) Integer.parseInt((String) this.getValueAt(row,col),radix): (short) this.getValueAt(row,col)
                        );
                        setCurrent(currentStart,currentEnd);
                    }
                }

            });
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return allData.get(currentStart + ((long) (row - 1)) * numberOfColumns + col) == null;
        }
    }
}
