package zen.ilgo.music;

import java.io.File;

import org.apache.log4j.Logger;

import zen.ilgo.music.ifaces.IMusicUI;
import zen.ilgo.music.ui.SwingUI;

/**
 * This class initializes the MagnaDownloader.
 *
 * @author ilgo May 6, 2009
 */
public class MagnaDownload {

	// the hard-coded directory where Magnatune albums will be saved
	private static Logger log = Logger.getLogger("zen.ilgo.music");
	public static File BASE_DIR = new File(System.getProperty("user.home"), "Music/Magnatune");
	private IMusicUI ui;

	public MagnaDownload(String[] args) {
		checkDirStructure();
	}
	
	public void play() {
		ui = new SwingUI(BASE_DIR);
		ui.show();
	}
	
	/**
	 * Making sure "~/Music/Magnatune" is existing.
	 */
	private void checkDirStructure() {
		if (!BASE_DIR.exists()) {
			if (!BASE_DIR.mkdirs()) {
				log.error("Cannot create Home Dir Structure :" + BASE_DIR.getAbsolutePath());
			}
		}
	}

	public static void main(String[] args) {
		MagnaDownload music = new MagnaDownload(args);
		music.play();
	}
}
