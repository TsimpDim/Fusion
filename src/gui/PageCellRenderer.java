package gui;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import control.PdfWorkspace;

class PageCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = -2628784840588878213L;
	private PdfWorkspace works;
	
	public PageCellRenderer(PdfWorkspace works) {
		super();
		this.works = works;
	}
	
	public Component getTableCellRendererComponent(
                        JTable table, Object value,
                        boolean isSelected, boolean hasFocus,
                        int row, int column) {
		
        JLabel c = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        String pages = table.getValueAt(row, column).toString();
        ArrayList<Integer> availablePagesList = works.getAllFiles().get(row).getAvailablePages();
        String availablePagesString = String.valueOf(availablePagesList.get(availablePagesList.size() - 1));

        c.setToolTipText("<html><i>e.g<i> 1,2,4-10,16,17-19" + "<br><br>"
        		+ "<div width=\"200\" style=\"word-break:break-all\"><br>"
        		+ "<strong>Available pages : <strong>" + availablePagesString + "</div>"
        		+ "<strong>Selected pages : <strong>" + pages + "</div>"
        		+ "</html>");
        
        return c;
    }
}
