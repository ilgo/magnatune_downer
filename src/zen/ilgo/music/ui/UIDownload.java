/**
 * 
 */
package zen.ilgo.music.ui;

import zen.ilgo.music.album.MagnatuneAlbum;
import zen.ilgo.music.album.MagnatuneSong;
import zen.ilgo.music.ifaces.IAlbum;

/**
 * This object will download the album and display it in the GUI.
 * it will update the song table with the new song infos. 
 * It also ensures that the Album and the GUI objects stay 
 * loosely coupled.
 * 
 * @author ilgo
 * @since Thursday, May 28 2009
 */
public class UIDownload implements Runnable {

	private IAlbum album;
	private MusicModel songModel;
	
	public UIDownload(IAlbum album, MusicModel songModel) {
		this.album = album;
		this.songModel = songModel;
	}
	
	@Override
	public String toString() {
		return ((MagnatuneAlbum)album).getUrl();
	}

	@Override
	public void run() {
		
		cleanSongModel();
		updateSongModel();
		album.downloadSongs();
	}
	
	private void cleanSongModel() {
		
		for (int n = songModel.getRowCount() - 1; n >= 0; n--){
			songModel.removeRow(n);
		}		
	}
	
	private void updateSongModel() {

		int idx = 0;
		for (MagnatuneSong song : album.getSongs()) {
			songModel.addRow(new Object[2]);
			MusicProgressBar songProgress = new MusicProgressBar(songModel, idx);
			songModel.setValueAt(song.getSongName().replaceAll("%20", " "),
					idx, 0);
			songModel.setValueAt(songProgress, idx, 1);
			album.addWgetChangeListener(songProgress);
			idx++;
		}
	}

}
