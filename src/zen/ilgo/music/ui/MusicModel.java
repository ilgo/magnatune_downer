package zen.ilgo.music.ui;

import javax.swing.table.DefaultTableModel;

/**
 *  Manages the Data for a MusicTable.
 *
 * @author ilgo May 15, 2009
 */
public class MusicModel extends DefaultTableModel {
	
	private static final long serialVersionUID = 2431066184620443603L;
	private final String name;
	
	public MusicModel(int row, int col, String name) {
		super(row,col);
		this.name = name;
	}
	
	@Override
	public String getColumnName(int idx) {
		return idx == 0 ? name : "Status";
	}
}
