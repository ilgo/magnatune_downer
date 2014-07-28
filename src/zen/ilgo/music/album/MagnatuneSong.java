package zen.ilgo.music.album;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import zen.ilgo.tools.wget.Wget;

/**
 * The MagnatuneSong class is a light wrapper around Wget.
 * 
 * @author ilgo (ilgo711@gmail.com)
 * @since Apr 12, 2009
 */
public class MagnatuneSong extends Wget implements Comparable<MagnatuneSong>{

	private final OutputStream target;
	
	public MagnatuneSong(URL wgetUrl, OutputStream target) {
		super(wgetUrl, target);
		this.target = target;
	}

	public String getSongName() {
		return name;
	}

	public void downloadSong() {
		Thread get = new Thread(this);
		get.start();
	}

	public boolean cancelSong() {
		return cancelWget();
	}
	
	public boolean closeTarget() {
		
		boolean closed = true;
		if (target != null) {
			try {
				target.close();				
			} catch (IOException ioe) {
				closed = false;
			}
		}
		return closed;
	}

	@Override
	public int compareTo(MagnatuneSong that) {
		return this.name.compareTo(that.name);
	}
}
