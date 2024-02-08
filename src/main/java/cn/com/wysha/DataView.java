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
    private long currentStart;
    private long currentEnd;
    private JPanel contentPane;
    private JPanel valuesJPanel;
    private JPanel charsJPanel;
    private JLabel label;
    private JButton jumpButton;
    private JButton saveButton;
    private JButton readButton;
    private JButton overWriteButton;

    public DataView(File current, int radix, int numberOfColumns) {
        this.current = current;
        setContentPane(contentPane);
        label.setText("radix : " + radix);
        this.numberOfColumns = numberOfColumns;
        this.radix = radix;
        this.names = new String[numberOfColumns+1];
        names[0]="index";
        for (int i = 1; i <= numberOfColumns; i++) {
            names[i] = Integer.toString(i-1, radix);
        }
        setTitle(current.getAbsolutePath());
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
        DefaultTableModel listJTableModel = new DefaultTableModel();
        JTable list = new JTable(listJTableModel);
        list.setEnabled(false);
        setSize(dimension.width / 2, dimension.height / 2);
        readButton.addActionListener(e -> {
            Choose choose = new Choose();
            choose.setVisible(true);
            if (choose.isChoose()) {
                long[] rs = choose.getValue();
                read(rs[0], rs[1]);
            }
        });
        jumpButton.addActionListener(e -> {
            Choose choose = new Choose();
            choose.setVisible(true);
            if (choose.isChoose()) {
                long[] rs = choose.getValue();
                setCurrent(rs[0], rs[1]);
            }
        });
        saveButton.addActionListener(e -> {
            Choose choose = new Choose();
            choose.setVisible(true);
            if (choose.isChoose()) {
                long[] rs = choose.getValue();
                write(rs[0], rs[1]);
            }
        });
        overWriteButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setMultiSelectionEnabled(true);
            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                overWrite(file);
            }
        });
    }

    private void setCurrent(long start, long end) {
        currentStart = start;
        currentEnd = end;
        int numberOfRows = (int) ((end - start) / numberOfColumns) + 1;
        String[][] bytes = new String[numberOfRows][numberOfColumns + 1];
        String[][] chars = new String[numberOfRows][numberOfColumns + 1];
        int row = -1;
        int col = numberOfColumns+1;
        for (long i = start; i < end; ++i) {
            System.out.println(current.getName()+"\tsetCurrent\t"+i+"\t/\t"+end);
            if (col > numberOfColumns) {
                ++row;
                col = 1;
                bytes[row][0] = Long.toString(start+ (long) row *numberOfColumns, radix);
                chars[row][0] = Long.toString(start+ (long) row *numberOfColumns, radix);
            }
            Short b = allData.get(i);
            if (b == null) {
                bytes[row][col] = null;
                chars[row][col] = null;
            } else {
                bytes[row][col] = Integer.toString(b, radix);
                chars[row][col] = String.valueOf((char) (short) b);
            }
            ++col;
        }
        bytes[numberOfRows-1][0] = Long.toString(start+ (long) (numberOfRows-1) *numberOfColumns, radix);
        chars[numberOfRows-1][0] = Long.toString(start+ (long) (numberOfRows-1) *numberOfColumns, radix);
        valuesJTableModel.setDataVector(bytes, names);
        charsJTableModel.setDataVector(chars, names);
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

    private void overWrite(File file) {
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(current, "rwd")) {
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                randomAccessFile.seek(currentStart);
                byte[] bytes=fileInputStream.readNBytes((int) (currentEnd-currentStart));
                randomAccessFile.write(bytes);
            }
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
                    int col = event.getColumn()-1;
                    if (row >= 0 && col >= 0) {
                        allData.put(
                                currentStart + ((long) (row)) * numberOfColumns + col,
                                this==valuesJTableModel? (short) Integer.parseInt((String) this.getValueAt(row,event.getColumn()),radix): (short) ((String)this.getValueAt(row,event.getColumn())).charAt(0)
                        );
                        setCurrent(currentStart,currentEnd);
                    }
                }

            });
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return col!=0&&allData.get(currentStart + ((long) row) * numberOfColumns + col-1) != null;
        }
    }
}
