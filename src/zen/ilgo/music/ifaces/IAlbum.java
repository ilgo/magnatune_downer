package zen.ilgo.music.ifaces;

import java.util.List;

import zen.ilgo.music.album.M3uException;
import zen.ilgo.music.album.MagnatuneSong;
import zen.ilgo.tools.wget.IWgetChangeListener;
import zen.ilgo.tools.wget.IWgetChanges;

/**
 * An Album takes care of all the songs in it.
 * An album needs to tie togehter all initalizing of songs,
 * logging the state of each song and clean up all 
 * allocated resources after the download is complete.
 * Furthermore in case that a download is cancelled
 * it needs to properly shutdown all the running threads,
 *
 * @author ilgo May 6, 2009
 */
public interface IAlbum extends IWgetChangeListener, IWgetChanges, Runnable{

	/**
	 * gets the m3u
	 * @throws M3uException 
	 */
	void initAlbum() throws M3uException;
	
	/**
	 * get the albums name.
	 * 
	 * @return the album name
	 */
	String getAlbumName();
	
	/**
	 * get all Songs, for their names;
	 * 
	 * @return a list of Songs
	 */
	List<MagnatuneSong> getSongs();
	
	/**
	 * Start the songs as Threads and download.
	 */
	void downloadSongs();
	
	/**
	 * Interrupt all downloads.
	 * 
	 * @return true if all songs were cancelled
	 */
	boolean cancelSongs();
	
	/**
	 * Clean up all resources.
	 */
	void dispose();
}
