package zen.ilgo.music.album;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class M3uParser {

    private Map<File, URL> content;
    private String subDir = null;

	private static Logger log = Logger.getLogger("zen.ilgo.music");

    public M3uParser(File baseDir, File m3u) throws M3uException {

        content = new HashMap<File, URL>();
        parsePlayList(m3u, baseDir);
    }

    public Map<File, URL> getContent() {
        return content;
    }

    public String getSubDir() {
        return subDir;
    }

    private void parsePlayList(File m3u, File baseDir) throws M3uException {

        String info;
        String location;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(m3u));
            if (br.readLine().equals("#EXTM3U")) {
                while ((info = br.readLine()) != null && info.startsWith("#EXTINF")) {
                    location = br.readLine();
                    log.debug(location);
                    File destination = new File(baseDir, subDir(info));
                    destination = new File(destination, fileName(location));
                    URL url = new URL(location);
                    content.put(destination, url);
                }
            }
        } catch (IOException e) {
            throw new M3uException(e);
        } finally {
            try {
                br.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Extract the song and replace %20 with underscores
     * http://he3.magnatune.com/all/02-Fountain%20of%20life-Artemis.mp3
     * 
     * @param location
     * @return fileName 02-Fountain_of_life-Artemis.mp3
     */
    private String fileName(String location) {

        int pos = location.lastIndexOf("/");
        String fileName = location.substring(pos + 1);
        return fileName.replaceAll("%20", "_");
    }

    /**
     * extract Band/Album from an info string
     * #EXTINF:202,Artemis - Gravity - Fountain of life (3:22)
     * 
     * @param info
     * @return subDir Artemis/Gravity
     */
    private String subDir(String info) {
        if (subDir == null) {
            int pos = info.indexOf(",");
            String data = info.substring(pos + 1);
            String[] parts = data.split("-");
            String band = parts[0].trim();
            String album = parts[1].trim();
            subDir = band + "/" + album;
        }
        return subDir;

    }
}
