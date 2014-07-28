package zen.ilgo.music.album;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import zen.ilgo.tools.wget.IWgetChangeListener;
import zen.ilgo.tools.wget.Wget;
import zen.ilgo.tools.wget.WgetChangeEvent;

public class AlbumTest {

	static MagnatuneAlbum album;
	static URL url;
	static File dir; 
	static Map<String, Long> wwwFiles;
	static int[] states;
	static TotalListener total, sub1, sub2, sub3, sub4, sub5;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		url = new URL("http://zen.magnatune.com/amelia-danca/hifi.m3u");
		dir = new File("/home/ilgo/Music/Magnatune");
				
		wwwFiles = new HashMap<String, Long>(5);
		for (File file : new File("/var/www/test/all").listFiles()) {
			long size = file.length();
			wwwFiles.put(file.getName(), size);
		}
		
		total = new TotalListener();
		sub1 = new TotalListener();
		sub2 = new TotalListener();
		sub3 = new TotalListener();
		sub4 = new TotalListener();
		sub5 = new TotalListener();
	}
	
	@Test
	public void testGetAlbumName() throws M3uException {
		
		album = new MagnatuneAlbum(dir, url);
		String albumName = album.getAlbumName();
		
		System.out.println("Album Name = " + albumName);
		
		assertEquals(albumName, "Amelia Cuni/Danza D Amore");
	}
	
	@Test
	public void testGetSongs() {
		List<MagnatuneSong> songs = album.getSongs();
		
		System.out.println("Song Count = " + songs.size());
		
		assertEquals(songs.size(), 5);
	}

	@Test
	public void testDownloadSongs() throws Exception {
		
		album.downloadSongs();
		Thread.sleep(3000);

		File[] files = new File(dir + "/" + album.getAlbumName()).listFiles();
		for (File file : files) {
			long size = file.length();
			String name = file.getName();
			assertTrue(size == wwwFiles.get(name));				
		}		
	}
	

	@Test
	public void testTotalFireWgetChangeEvent() throws Exception {
		
		album = new MagnatuneAlbum(dir, url);
	
		album.addWgetChangeListener(total);
		album.downloadSongs();
		Thread.sleep(3000);

		states = total.getStates();
		
		System.out.println(Arrays.toString(states));
		assertTrue(Arrays.equals(states, new int[]{5, 301, 1, 0}));
	}
	
	@Test
	public void testSubFireWgetChangeEvent() throws Exception {
		
		album = new MagnatuneAlbum(dir, url);
	
		album.addWgetChangeListener(total);
		album.addWgetChangeListener(sub1);
		album.addWgetChangeListener(sub2);
		album.addWgetChangeListener(sub3);
		album.addWgetChangeListener(sub4);
		album.addWgetChangeListener(sub5);
		album.downloadSongs();
		Thread.sleep(3000);
		
		states = total.getStates();		
		System.out.println("tota: " + Arrays.toString(states));
		states = sub1.getStates();		
		System.out.println("sub1: " + Arrays.toString(states));
		states = sub2.getStates();		
		System.out.println("sub2: " + Arrays.toString(states));
		states = sub3.getStates();		
		System.out.println("sub3: " + Arrays.toString(states));
		states = sub4.getStates();		
		System.out.println("sub4: " + Arrays.toString(states));
		states = sub5.getStates();		
		System.out.println("sub5: " + Arrays.toString(states));	
		
		album.dispose();
	}

	@Test
	public void testCancelSongs() throws M3uException {
		
		album = new MagnatuneAlbum(dir, url);
		album.downloadSongs();
		try {
			Thread.sleep(20);
		} catch (InterruptedException e) {
		}
		album.cancelSongs();
		File[] files = new File(dir + "/" + album.getAlbumName()).listFiles();
		boolean failed = false;
		for (File file : files) {
			long size = file.length();
			String name = file.getName();
			failed = (size != wwwFiles.get(name)) ? true : false;
		}
		assertTrue(failed == true);
	}

	@Test
	public void testDispose() {
		int count = 0;
		for (MagnatuneSong song : album.getSongs()) {
			count += song.closeTarget() == true ? 1 : 0;
		}
		assertEquals(count, 5);
	}

	static class TotalListener implements IWgetChangeListener {

		int content, read, total, fail;
		
		@Override
		public void wgetStateChanged(WgetChangeEvent e) {
						
			int state = e.getWgetState();
			switch (state) {
			case Wget.WGETSTATE_CONTENT:
				content ++;
				break;
			case Wget.WGETSTATE_READ:
				read ++;
				break;
			case Wget.WGETSTATE_TOTAL:
				total ++;
				break;
			case Wget.WGETSTATE_FAIL:
				fail ++;
			}
		}
			
		public int[] getStates() {
			int[] states = new int[4];
			states[0] = content;
			states[1] = read;
			states[2] = total;
			states[3] = fail;
			return states;
		}		
	}
}
