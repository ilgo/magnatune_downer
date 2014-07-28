package zen.ilgo.music.ui;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 * A custom table allowing to display ProgressBars in cells.
 *
 * @author ilgo May 15, 2009
 */
public class MusicTable extends JTable {

	private static final long serialVersionUID = -322408456758420896L;
	
	TableCellRenderer progressRenderer = new ProgressRenderer();
	
	public MusicTable() {
		initTable();
	}
	
	public MusicTable(TableModel model) {
		super(model);
		initTable();
		setRenderer();
	}
	
	@Override
	public void tableChanged(TableModelEvent e) {
		super.tableChanged(e);
	}
	
	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}
	
	private void initTable() {
		setColumnSelectionAllowed(false);
		setCellSelectionEnabled(false);
		setRowHeight(22);		
	}
	
	public void setRenderer() {
		getColumnModel().getColumn(1).setCellRenderer(new ProgressRenderer());
	}
	
	@Override
	public TableCellRenderer getCellRenderer(int row, int col) {
		
		// the ProgressBar should be second colum, that is index 1
		if (col == 1) {
			return progressRenderer;
		}else {
			return super.getCellRenderer(row, col);
		}
	}
	
	/**
	 * A custom renderer to display a ProgressBar inside a JTable cell.
	 * 
	 * @author ilgo
	 * @since May 15, 2009
	 */
	private static class ProgressRenderer extends DefaultTableCellRenderer{

		static final long serialVersionUID = 464491752354080159L;

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object obj, boolean arg2, boolean arg3, int arg4, int arg5) {

			return (Component)obj;
		}		
	}
}
