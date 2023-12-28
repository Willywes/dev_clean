import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

public class Main extends JFrame {

    String[] foldersToFind = {"vendor", "node_modules"};

    // Model for the table
    DefaultTableModel tableModel;

    public Main() {
        // Set up the JFrame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        // Create a file chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // Show the file chooser dialog
        int result = fileChooser.showOpenDialog(this);

        // Check if the user selected a directory
        if (result == JFileChooser.APPROVE_OPTION) {
            String selectedDirectory = fileChooser.getSelectedFile().getAbsolutePath();
            // Initialize the table
            initializeTable();

            // Find and show folders
            findAndShowFolders(selectedDirectory);
        } else {
            System.out.println("No directory selected. Exiting...");
            System.exit(0);
        }
    }

    private void initializeTable() {
        // Initialize the table model
        tableModel = new DefaultTableModel();
        tableModel.addColumn("Folder Path");
        tableModel.addColumn("Action");

        // Create the table
        JTable table = new JTable(tableModel);

        // Create a button column with actions
        ButtonColumn buttonColumn = new ButtonColumn(table, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Integer modelRow = table.convertRowIndexToModel(table.getSelectedRow());
                String folderPath = (String) tableModel.getValueAt(modelRow, 0);
                if (confirmDelete(folderPath)) {
                    deleteFolder(folderPath);
                    // Remove the row from the table after deletion
                    tableModel.removeRow(modelRow);
                }
            }
        }, 1);

        // Set up the frame layout
        JScrollPane scrollPane = new JScrollPane(table);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        // Show the frame
        setVisible(true);
    }

    private boolean confirmDelete(String folderPath) {
        int option = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the folder?\n" + folderPath,
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        return option == JOptionPane.YES_OPTION;
    }

    private void findAndShowFolders(String directory) {
        findAndShowFoldersRecursive(new File(directory));
    }

    private void findAndShowFoldersRecursive(File directory) {
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (file.getName().equals("vendor") || file.getName().equals("node_modules")) {
                        // Add the folder path to the table
                        Vector<Object> rowData = new Vector<>();
                        rowData.add(file.getAbsolutePath());
                        rowData.add("Delete");
                        tableModel.addRow(rowData);
                    } else {
                        findAndShowFoldersRecursive(file);
                    }
                }
            }
        }
    }

    private void deleteFolder(String folderPath) {
        File folder = new File(folderPath);
        if (folder.exists() && folder.isDirectory()) {
            deleteRecursive(folder);
            System.out.println("Folder deleted: " + folderPath);
        }
    }

    private void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            File[] files = fileOrDirectory.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteRecursive(child);
                }
            }
        }
        fileOrDirectory.delete();
    }

    public static void main(String[] args) {
        // Run the GUI on the event dispatch thread
        SwingUtilities.invokeLater(Main::new);
    }

    // Class to create button column in JTable
    static class ButtonColumn extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener {
        JTable table;
        JButton renderButton;
        JButton editButton;
        String text;

        public ButtonColumn(JTable table, Action action, Integer column) {
            this.table = table;
            renderButton = new JButton();
            editButton = new JButton();
            editButton.setFocusPainted(false);
            editButton.addActionListener(this);

            TableColumnModel columnModel = table.getColumnModel();
            columnModel.getColumn(column).setCellRenderer(this);
            columnModel.getColumn(column).setCellEditor(this);
            columnModel.getColumn(column).setPreferredWidth(80); // Adjust the button column width

            renderButton.setAction(action);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (hasFocus) {
                renderButton.setForeground(table.getForeground());
                renderButton.setBackground(UIManager.getColor("Button.background"));
            } else if (isSelected) {
                renderButton.setForeground(table.getSelectionForeground());
                renderButton.setBackground(table.getSelectionBackground());
            } else {
                renderButton.setForeground(table.getForeground());
                renderButton.setBackground(UIManager.getColor("Button.background"));
            }

            renderButton.setText((value == null) ? "" : value.toString());
            return renderButton;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            text = (value == null) ? "" : value.toString();
            editButton.setText(text);
            return editButton;
        }

        @Override
        public Object getCellEditorValue() {
            return text;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fireEditingStopped();
            // Perform the action when the button is clicked
            ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, text);
            ((AbstractAction) renderButton.getAction()).actionPerformed(event);
        }
    }
}
