package zen.ilgo.music.ui;

import javax.swing.JProgressBar;
import javax.swing.table.DefaultTableModel;

import zen.ilgo.tools.wget.IWgetChangeListener;
import zen.ilgo.tools.wget.Wget;
import zen.ilgo.tools.wget.WgetChangeEvent;

public class MusicProgressBar extends JProgressBar implements IWgetChangeListener{

	private static final long serialVersionUID = -2261031159233538536L;
	
	private long total;
	private int idx;
	private MusicModel model;
	
	public MusicProgressBar(MusicModel model, int idx) {
		setStringPainted(true);
		this.model = model;
		this.idx = idx;
	}
	
	@Override
	public void wgetStateChanged(WgetChangeEvent e) {
		
		// update the album state
		int state = e.getWgetState();
		switch (state) {
		case Wget.WGETSTATE_CONTENT:
			total = e.getBytes();
			setMaximum(100);
			setMinimum(0);
			break;
		case Wget.WGETSTATE_READ:
			long bytesRead = e.getBytes();
			int percentage = (int) (bytesRead * 100 / total);
			setValue(percentage);
			setString(String.format("%d%s", percentage, '%'));
			break;
		case Wget.WGETSTATE_TOTAL:
			setString("Success");
			break;
		case Wget.WGETSTATE_FAIL:
			setString("Failure");
		}
		((DefaultTableModel)model).fireTableCellUpdated(idx, 1);
	}
}