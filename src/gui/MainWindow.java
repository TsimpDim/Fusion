package gui;

import control.PdfFile;
import control.PdfWorkspace;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;

public class MainWindow extends JFrame{


	private static final long serialVersionUID = -6007278116878081383L;

	PdfWorkspace workspace = null;

	JPanel container;

	JFileChooser fileChooser;

	JTable fileTable;
	JScrollPane fileTablePane;
	PdfFileTableModel tableModel;
	JPopupMenu tableMenu;

	JMenuBar menuBar;
	JMenu selectionMenu;
	JMenu addMenu;
	JMenu editMenu;

	JMenuItem addFiles;
	JMenuItem mergeFiles;
	JMenuItem watermarkFiles;


    ImageIcon deleteIcon = new ImageIcon(getClass().getResource("/res/actions/delete.png"));
	ImageIcon moveUpIcon = new ImageIcon(getClass().getResource("/res/actions/move-up.png"));
	ImageIcon moveDownIcon = new ImageIcon(getClass().getResource("/res/actions/move-down.png"));
	ImageIcon dupliIcon = new ImageIcon(getClass().getResource("/res/actions/duplicate.png"));
	ImageIcon undoIcon = new ImageIcon(getClass().getResource("/res/actions/undo.png"));
	ImageIcon openIcon = new ImageIcon(getClass().getResource("/res/actions/open.png"));

	DeleteRowsAction deleteSelectedRowsAction = new DeleteRowsAction("Delete selected file(s)", deleteIcon ,null, null, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
	MoveRowsUpAction moveSelectionUpAction = new MoveRowsUpAction("Move file up", moveUpIcon, null, null, KeyStroke.getKeyStroke(KeyEvent.VK_UP, ActionEvent.ALT_MASK));
	MoveRowsDownAction moveSelectionDownAction = new MoveRowsDownAction("Move file down", moveDownIcon, null, null, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, ActionEvent.ALT_MASK));
	DuplicateRowsAction duplicateSelectionAction = new DuplicateRowsAction("Duplicate file(s)", dupliIcon, null, null, KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
	UndoDeletionAction undoDeletionAction = new UndoDeletionAction("Undo deletion", undoIcon, null, null, KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
	OpenFileAction openFileAction = new OpenFileAction("Open selected file(s)", openIcon, null, null, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0));

	public MainWindow(PdfWorkspace works) {

	    try {
	        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    }catch(Exception ex) {
	        ex.printStackTrace();
	    }

	    // Base declarations
		workspace = works;

		container = new JPanel();
		ButtonListener buttonListener = new ButtonListener();
		menuBar = new JMenuBar();

		// Add Menu
		addMenu = new JMenu("Add");
		addMenu.setMnemonic(KeyEvent.VK_O);
		addFiles = new JMenuItem("Add files");
		addFiles.addActionListener(buttonListener);
		addFiles.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		ImageIcon addIcon = new ImageIcon(getClass().getResource("/res/actions/add.png"));
		addFiles.setIcon(addIcon);

		// Selection Menu
		selectionMenu = new JMenu("Selection");
		selectionMenu.setMnemonic(KeyEvent.VK_S);


		// Edit Menu
		editMenu = new JMenu("Edit");
		editMenu.setMnemonic(KeyEvent.VK_E);

		mergeFiles = new JMenuItem("Merge files");
		mergeFiles.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, ActionEvent.CTRL_MASK));
		ImageIcon mergeIcon = new ImageIcon(getClass().getResource("/res/actions/merge.png"));
		mergeFiles.setIcon(mergeIcon);

		watermarkFiles = new JMenuItem("Watermark files");
		ImageIcon wtrmkIcon = new ImageIcon(getClass().getResource("/res/actions/watermark.png"));
		watermarkFiles.setIcon(wtrmkIcon);

		fileChooser = new JFileChooser();
		fileChooser.setMultiSelectionEnabled(true);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("PDF Documents", "pdf");
		fileChooser.setFileFilter(filter);

		mergeFiles.addActionListener(buttonListener);
		watermarkFiles.addActionListener(buttonListener);


		// Table
		fileTable = new JTable();
		tableModel = new PdfFileTableModel(null);
		fileTable.setFillsViewportHeight(true);
		fileTable.setModel(tableModel);

		// Remove default ENTER-KEY behavior
		fileTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
				.put(KeyStroke.getKeyStroke("ENTER"), "none");

		// Drag n Drop
		fileTable.setDropTarget(new DropTarget() {
			public synchronized void drop(DropTargetDropEvent evt) {
				try {
					evt.acceptDrop(DnDConstants.ACTION_COPY);
					Boolean allFilesCorrect = true;
					ArrayList<File> droppedFiles = (ArrayList<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

					for (File file : droppedFiles) {

						// Get extension
						String extension = " ";
						int i = file.getName().lastIndexOf('.');
						if (i >= 0) { extension = file.getName().substring(i+1); } // Has extension
						else { allFilesCorrect = false; } // Does not have extension

						// Check if PDF and add to workspace
						if(extension.equals("pdf")) {
							works.addFileToWorkspace(new PdfFile(file, PdfWorkspace.totalFiles));
							tableModel.updateData(workspace.getAllFiles());
						}else{
							allFilesCorrect = false;
						}
					}

					if(!allFilesCorrect)
						JOptionPane.showMessageDialog(null, "Could not add all files\nOnly PDF files can be added into the workspace!", "Error!", JOptionPane.ERROR_MESSAGE);

					evt.dropComplete(true);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		PageCellRenderer leftAlignedRenderer = new  PageCellRenderer(works);
		leftAlignedRenderer.setHorizontalAlignment(JLabel.LEFT);

		fileTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		fileTable.getColumnModel().getColumn(0).setPreferredWidth(20);
		fileTable.getColumnModel().getColumn(0).setCellRenderer(leftAlignedRenderer);
		fileTable.getColumnModel().getColumn(1).setPreferredWidth(741);
		fileTable.getColumnModel().getColumn(2).setPreferredWidth(60);
		fileTable.getColumnModel().getColumn(3).setPreferredWidth(60);
		fileTable.getColumnModel().getColumn(2).setCellRenderer(new PageCellRenderer(works));
		fileTable.setAutoCreateRowSorter(true);
		fileTablePane = new JScrollPane(fileTable);

		// Table Double-Click
		fileTable.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent mouseEvent) {
				JTable table =(JTable) mouseEvent.getSource();
				Point point = mouseEvent.getPoint();
				int row = table.rowAtPoint(point);
				int col = table.columnAtPoint(point);

				// If double click
				// And double click not on the editable columns (pages & include)
				if (mouseEvent.getClickCount() == 2 && col != 2 && col != 3) {
					int selectedRow = fileTable.getSelectedRow();
					if(selectedRow > -1) {
						try{
							Desktop.getDesktop().open(new File(workspace.getFile(row).getPath()));
						}catch(java.io.IOException | java.lang.IllegalArgumentException ex){
							JOptionPane.showMessageDialog(null, "Could not open file.", "Error!", JOptionPane.ERROR_MESSAGE);
						}
					}

				}
			}
		});

		// Table Right-Click Menu
		tableMenu = new JPopupMenu();

		tableMenu.add(deleteSelectedRowsAction);
		tableMenu.add(moveSelectionUpAction);
		tableMenu.add(moveSelectionDownAction);
		tableMenu.add(duplicateSelectionAction);
		tableMenu.add(undoDeletionAction);
		tableMenu.add(new JSeparator());
		tableMenu.add(openFileAction);

		fileTable.setComponentPopupMenu(tableMenu);


	    // Component setup
		addMenu.add(addFiles);

		editMenu.add(mergeFiles);
		editMenu.add(watermarkFiles);

		selectionMenu.add(deleteSelectedRowsAction);
		selectionMenu.add(moveSelectionUpAction);
		selectionMenu.add(moveSelectionDownAction);
		selectionMenu.add(duplicateSelectionAction);
		selectionMenu.add(undoDeletionAction);
		selectionMenu.add(new JSeparator());
		selectionMenu.add(openFileAction);

		menuBar.add(addMenu);
		menuBar.add(editMenu);
		menuBar.add(selectionMenu);

	    container.setLayout(new BorderLayout());
	    container.add(menuBar, BorderLayout.PAGE_START);
	    container.add(fileTablePane, BorderLayout.CENTER);

		this.setContentPane(container);

		this.setTitle("PDFusion Workspace");
		ArrayList<Image> icons = new ArrayList<>();
		icons.add(new ImageIcon(getClass().getResource("/res/logo/PDFusion_logo_16.png")).getImage());
		icons.add(new ImageIcon(getClass().getResource("/res/logo/PDFusion_logo_20.png")).getImage());
		icons.add(new ImageIcon(getClass().getResource("/res/logo/PDFusion_logo_32.png")).getImage());
		icons.add(new ImageIcon(getClass().getResource("/res/logo/PDFusion_logo_40.png")).getImage());
		icons.add(new ImageIcon(getClass().getResource("/res/logo/PDFusion_logo_64.png")).getImage());
		icons.add(new ImageIcon(getClass().getResource("/res/logo/PDFusion_logo_128.png")).getImage());
		this.setIconImages(icons);

		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(1000,500);
	}

	class ButtonListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {

			if (arg0.getSource().equals(addFiles)) { // Open files and add them into the workspace

				int returnVal = fileChooser.showOpenDialog(MainWindow.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File[] new_files = fileChooser.getSelectedFiles();


					for (File file : new_files) {

						String curPath = file.getPath();
						int fileIndex = PdfWorkspace.totalFiles;

						PdfFile newPDF = new PdfFile(curPath, true, fileIndex);
						workspace.addFileToWorkspace(newPDF);

					}
					tableModel.updateData(workspace.getAllFiles());
				}

			} else if (arg0.getSource().equals(mergeFiles)) { // Merge files

				fileChooser.setSelectedFile(new File("export.pdf")); // Sets default filename

				int returnVal = fileChooser.showSaveDialog(MainWindow.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {

					String destination = fileChooser.getSelectedFile().getPath();

					if (!destination.endsWith(".pdf"))
						destination += ".pdf";


					// Initialize progress bar
					ResultsWindow progBar = new ResultsWindow(PdfWorkspace.totalFilesToInclude, "Preparing files...", destination);
					workspace.mergeFiles(destination, progBar);
				}
			} else if (arg0.getSource().equals(watermarkFiles)){ // Watermark files

				new WatermarkWindow(workspace, fileTable.getSelectedRows());
			}
		}
	}

	/**
	 * Action for deleting selected rows from the workspace only if it is possible
	 */
	class DeleteRowsAction extends AbstractAction {

		private static final long serialVersionUID = 8380768403177959272L;

		public DeleteRowsAction(String text, ImageIcon icon,
								String desc, Integer mnemonic, KeyStroke accelerator) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
			putValue(ACCELERATOR_KEY, accelerator);
		}

		public void actionPerformed(ActionEvent e){

			int selectedRow = fileTable.getSelectedRow();
			if(selectedRow > -1) {
				int[] selectedRows = fileTable.getSelectedRows();
                int[] selectedRowsCRIM = new int[selectedRows.length]; // SelectedRowsConvertedRowIndexModel
                for(int i = 0; i < selectedRows.length; i++)
                    selectedRowsCRIM[i] = fileTable.convertColumnIndexToModel(selectedRows[i]);

				if(workspace.removeFilesFromWorkspace(selectedRowsCRIM)) {
					tableModel.fireTableRowsDeleted(selectedRows[0], selectedRows[selectedRows.length - 1]);

					// If the deleted file was the only file then we have nothing to select
					if(workspace.totalFiles == 0)
						return;

					try{
						fileTable.setRowSelectionInterval(selectedRow, selectedRow);
					}catch(java.lang.IllegalArgumentException er){
						fileTable.setRowSelectionInterval(selectedRow -1, selectedRow -1);
					}
				}
			}
		}
	}

	/**
	 * Action for moving selected rows higher on the workspace only if it is possible
	 */
	class MoveRowsUpAction extends AbstractAction{

		private static final long serialVersionUID = 3522968040593462776L;

		public MoveRowsUpAction(String text, ImageIcon icon,
								String desc, Integer mnemonic, KeyStroke accelerator) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
			putValue(ACCELERATOR_KEY, accelerator);
		}

		public void actionPerformed(ActionEvent e){
			int selectedRow = fileTable.getSelectedRow();
			if(selectedRow > 0) {
				int[] selectedRows = fileTable.getSelectedRows();
				if(workspace.moveFilesUp(selectedRows)) {
					tableModel.fireTableDataChanged();
					fileTable.setRowSelectionInterval(selectedRows[0] - 1, selectedRows[selectedRows.length - 1] - 1); // Move selection upwards
				}
			}
		}
	}

	/**
	 * Action for moving selected rows lower on the workspace only if it is possible
	 */
	class MoveRowsDownAction extends AbstractAction{

		private static final long serialVersionUID = -166008514298037644L;

		public MoveRowsDownAction(String text, ImageIcon icon,
								  String desc, Integer mnemonic, KeyStroke accelerator) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
			putValue(ACCELERATOR_KEY, accelerator);
		}

		public void actionPerformed(ActionEvent e){
			int selectedRow = fileTable.getSelectedRow();
			if(selectedRow > -1) {
				int[] selectedRows = fileTable.getSelectedRows();
				if(workspace.moveFilesDown(selectedRows)) {
					tableModel.fireTableDataChanged();
					fileTable.setRowSelectionInterval(selectedRows[0] + 1, selectedRows[selectedRows.length - 1] + 1); // Move selection downwards
				}
			}
		}
	}

	/**
	 * Duplicates selected rows
	 */
	class DuplicateRowsAction extends AbstractAction{

		private static final long serialVersionUID = -1381937240965848408L;

		public DuplicateRowsAction(String text, ImageIcon icon,
								   String desc, Integer mnemonic, KeyStroke accelerator) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
			putValue(ACCELERATOR_KEY, accelerator);
		}

		public void actionPerformed(ActionEvent e){
			int [] selectedRows = fileTable.getSelectedRows();
			int rows = PdfWorkspace.totalFiles;

			workspace.duplicateFiles(selectedRows);

			tableModel.fireTableDataChanged();
			fileTable.setRowSelectionInterval(rows, rows + selectedRows.length - 1);
		}
	}

	/**
	 * Undoes the last deletion
	 */
	class UndoDeletionAction extends AbstractAction{

		private static final long serialVersionUID = -5792826424475287932L;

		public UndoDeletionAction(String text, ImageIcon icon,
								  String desc, Integer mnemonic, KeyStroke accelerator) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
			putValue(ACCELERATOR_KEY, accelerator);
		}

		public void actionPerformed(ActionEvent e){
			workspace.undoPreviousDeletion();
			tableModel.fireTableDataChanged();
		}
	}

	/**
	 * Opens all the selected files
	 */
	 class OpenFileAction extends AbstractAction {

		private static final long serialVersionUID = 7657012530064869813L;

		public OpenFileAction(String text, ImageIcon icon,
							  String desc, Integer mnemonic, KeyStroke accelerator) {

			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
			putValue(ACCELERATOR_KEY, accelerator);
		}

		public void actionPerformed(ActionEvent e){
			int selectedRow = fileTable.getSelectedRow();
			if(selectedRow > -1) {
				int[] selectedRows = fileTable.getSelectedRows();

				for(int pageIdx : selectedRows){
					try{
						Desktop.getDesktop().open(new File(workspace.getFile(pageIdx).getPath()));
					}catch(java.io.IOException | java.lang.IllegalArgumentException ex){
						JOptionPane.showMessageDialog(null, "Could not open file.", "Error!", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
	}
}
