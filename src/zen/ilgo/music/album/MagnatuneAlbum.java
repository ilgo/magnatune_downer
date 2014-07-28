package zen.ilgo.music.album;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import zen.ilgo.music.ifaces.IAlbum;
import zen.ilgo.tools.wget.IWgetChangeListener;
import zen.ilgo.tools.wget.Wget;
import zen.ilgo.tools.wget.WgetChangeEvent;

/**
 * The AlbumModel keeps track of the total state change of all songs. It is the
 * Model for a ProgressMonitor, and will register itself with all the songs of
 * the album. The monitor will then display the overall percentage of what has
 * been downloaded. it will also send events to the ProgressBars that listen
 * for individual songs updat.
 * 
 * @author ilgo (ilgo711@gmail.com)
 * @since Apr 12, 2009
 */
public class MagnatuneAlbum implements IAlbum {

	private static Logger log = Logger.getLogger("zen.ilgo.music");

	private String bandAlbumDir;
	private final File baseDir;
	private final URL url;
	private File m3u;
	private List<MagnatuneSong> songs;
	private List<String> songNames;
	private long[] songRead;
	private Map<File, URL> m3uContent;
	private List<IWgetChangeListener> listeners;
	private long albumTotal;
	private long albumRead;
	private int songsComplete;
	private int songsFailed;
	private int albumState = -1;


	/**
	 * the Constructor.
	 * 
	 * @param baseDir the Directory where the Songs will be stored
	 * @param url the url for the hifi.m3u file on www.magnatune.com
	 * @throws M3uException if the m3u file cannot be parsed
	 */
	public MagnatuneAlbum(File baseDir, URL url) throws M3uException {

		this.baseDir = baseDir;
		this.url = url;
		log.info(new Date());
		log.info("Album :" + url);
	}

	/**
	 * Prepares the album for download.
	 * gets and parses the m3u file, creates the dir structure
	 * and allocates the necessary resources for the songs.
	 * 
	 * @throws M3uException if the m3u file cannot be parsed
	 */
	public void initAlbum() throws M3uException {
		getM3uFile(url);
		parseM3u();
		checkDirStructure();
	}

	public void run() {
		downloadSongs();
	}

	@Override
	public String getAlbumName() {
		return bandAlbumDir;
	}
	
	public String getUrl() {
		return url.toString();
	}

	/**
	 * get all the songs for this album.
	 * 
	 * @return List<MagnatuneSong> the songs
	 */
	public List<MagnatuneSong> getSongs() {
		prepareSongs();
		return songs;
	}

	/**
	 * Instantiate all the resources for the songs.
	 * The songs are sorted, so that the listeners 
	 * will be notified according to registration order.
	 * Registration index and alphabetical order correlate.
	 */
	private void prepareSongs() {

		songs = new ArrayList<MagnatuneSong>();
		Iterator<Entry<File, URL>> itFile = m3uContent.entrySet().iterator();
		while (itFile.hasNext()) {
			Entry<File, URL> entry = itFile.next();
			OutputStream local;
			try {
				local = new FileOutputStream(entry.getKey());
				URL url = entry.getValue();
				MagnatuneSong song = new MagnatuneSong(url, local);
				song.setBufferSize(4096);
				song.setEventInterval(25);
				song.addWgetChangeListener(this);
				songs.add(song);
			} catch (FileNotFoundException e) {
				log.error(e.getMessage());
			}
		}
		Collections.sort(songs);
		
		// later when sending events this list is
		// used to determine which listener is called
		songNames= new ArrayList<String>(songs.size());
		for (MagnatuneSong song : songs) {
			songNames.add(song.getSongName());
		}
		// will hold the total read bytes for each song
		songRead = new long[songs.size()];
	}

	/**
	 * Download all songs.
	 * Start the song threads, wait for the album to reach
	 * finish state and then close all song resources.
	 */
	public void downloadSongs() {

		for (MagnatuneSong song : songs) {
			song.downloadSong();
		}
		while (albumState < Wget.WGETSTATE_TOTAL) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.error("Download interrupted: " + e.getMessage());
			}
		}
		dispose();
	}

	@Override
	/**
	 * Interrupting to download this album.
	 */
	public boolean cancelSongs() {

		int cancelCount = 0;
		for (MagnatuneSong song : songs) {
			if (song.cancelSong()) {
				cancelCount++;
			}
		}
		return cancelCount == songs.size() ? true : false;
	}

	@Override
	/*
	 * to close the output streams passed to MagnatuneSong instances
	 */
	public void dispose() {

		for (MagnatuneSong song : songs) {
			song.closeTarget();
		}
		removeWgetChangeListeners();
	}

	@Override
	/**
	 * Multiple Wget instances will use this call-back.
	 * We have only one Album object.. so possibly
	 * albumState, albumRead and albumTotal can be corrupted.
	 */
	public synchronized void wgetStateChanged(WgetChangeEvent e) {

		// only Swing Gui will listen and display
		// individual song data...
		// therefore more than 1 listener in SwingUi
		String name = (String)e.getName();
		if (listeners != null && listeners.size() > 1) {
			int idx = songNames.indexOf(name);
			if (idx != -1) {
				WgetChangeEvent newEvent = null;
				if (e.getWgetState() == Wget.WGETSTATE_READ) {
					songRead[idx] += e.getBytes();
					newEvent = new WgetChangeEvent(name, e.getWgetState(), songRead[idx]);
				} else {
					newEvent = e;
				}
				listeners.get(idx + 1).wgetStateChanged(newEvent);
			}
		}
		// update the album state
		int state = e.getWgetState();
		long bytes = 0;
		switch (state) {
		case Wget.WGETSTATE_CONTENT:
			albumTotal += e.getBytes();
			bytes = albumTotal;
			break;
		case Wget.WGETSTATE_READ:
			albumRead += e.getBytes();
			bytes = albumRead;
			break;
		case Wget.WGETSTATE_TOTAL:
			songsComplete++;
			if (songsComplete != songs.size()) {
				// the album is not yet complete
				state = Wget.WGETSTATE_READ;
				albumState = state;
				bytes = albumRead;
			}
			log.debug("Done: " +e.getName().replaceAll("%20", " "));
			break;
		case Wget.WGETSTATE_FAIL:
			songsFailed++;
			songsComplete++;
			if (songsComplete != songs.size()) {
				state = Wget.WGETSTATE_READ;
				albumState = state;
				bytes = albumRead;
			}
			log.info("Failure: " +e.getName());
		}
		// and let the GUI know of the summarized album state
		fireWgetChangeEvent(state, bytes);		
	}

	@Override
	public void addWgetChangeListener(IWgetChangeListener listener) {
		if (listeners == null) {
			listeners = new ArrayList<IWgetChangeListener>();
		}
		listeners.add(listener);
	}

	@Override
	public void removeWgetChangeListeners() {
		if (listeners != null) {
			listeners.clear();
		}
	}

	@Override
	public void fireWgetChangeEvent(int state, long bytes) {

		if (listeners != null) {
			WgetChangeEvent e = new WgetChangeEvent(bandAlbumDir, state, bytes);
			listeners.get(0).wgetStateChanged(e);
		}
		if (state == Wget.WGETSTATE_TOTAL) {
			albumState = state;
			log.info("Finished: " + songs.size() + " songs - " + albumRead + " bytes");
		}
	}

	/**
	 * Download the m3u file.
	 * 
	 * @param url the m3u remote location
	 * @throws M3uException if we cant get the m3u file
	 */
	private void getM3uFile(URL url) throws M3uException {

		FileOutputStream fos = null;
		String prefix = System.getProperty("user.name") + "@"
				+ Calendar.getInstance().getTimeInMillis() + "_";
		try {
			m3u = File.createTempFile(prefix, ".m3u");
			fos = new FileOutputStream(m3u);
		} catch (IOException e) {
			throw new M3uException("Cannot retrieve remote m3u file");
		}
		Wget getM3u = new Wget(url, fos);
		Thread thread = new Thread(getM3u);
		thread.start();
		try {
			thread.join();
		} catch (InterruptedException ex) {
			log.error("Getting M3u interrupted: " + ex.getMessage());
		}
		try {
			if (fos != null) {
				fos.close();
			}
		} catch (IOException ioe) {
			// no need to catch this
		}
	}

	/**
	 * Parse the m3u file and create the directory.
	 * 
	 * @throws M3uException if the m3u file cannot be parsed
	 */
	private void parseM3u() throws M3uException {

		M3uParser parser = new M3uParser(baseDir, m3u);
		m3uContent = parser.getContent();
		bandAlbumDir = parser.getSubDir();
	}
	
	/**
	 * Creates the directory for album and song.
	 */
	private void checkDirStructure() {

		File bandAlbumDirPath = new File(baseDir, bandAlbumDir);
		if (!bandAlbumDirPath.exists()) {
			if (!bandAlbumDirPath.mkdirs()) {
				log.error("Cannot create Dir Structure :" + bandAlbumDirPath.getAbsolutePath());
			}
		}
	}
}
