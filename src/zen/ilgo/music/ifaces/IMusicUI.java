package zen.ilgo.music.ifaces;

import java.net.MalformedURLException;

import zen.ilgo.music.album.M3uException;

public interface IMusicUI {

	void show();

	void addAlbum(String strUrl) throws M3uException, MalformedURLException;
}
