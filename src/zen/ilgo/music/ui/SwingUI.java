package zen.ilgo.music.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;

import org.apache.log4j.Logger;

import zen.ilgo.music.MagnaDownload;
import zen.ilgo.music.album.M3uException;
import zen.ilgo.music.album.MagnatuneAlbum;
import zen.ilgo.music.ifaces.IMusicUI;

public class SwingUI implements IMusicUI {

	private final Logger log = Logger.getLogger("zen.ilgo.music");
	private final String ICON_LOCATION = "/zen/ilgo/music/resources/magnatune_16x16.png";

	private Image icon;
	private final ExecutorService albumQueue;
	private int albumCount = 0;

	private JFrame frame;
	private MusicModel albumModel;
	private MusicModel songModel;
	private JTable songTable;
	private JButton details;
	//private JButton newAlbum;
	private JScrollPane songPane;

	public SwingUI(File baseDir) {
		loadIcon();
		initComponents();
		albumQueue = Executors.newSingleThreadExecutor();
	}

	@Override
	public void show() {
		songPane.setVisible(false);
		frame.pack();
		frame.setVisible(true);
	}

	@Override
	public void addAlbum(String strUrl) throws MalformedURLException,
			M3uException {

		URL url = new URL(strUrl);
		MagnatuneAlbum album = new MagnatuneAlbum(MagnaDownload.BASE_DIR, url);
		MusicProgressBar albumProgress = new MusicProgressBar(albumModel,
				albumCount);
		albumProgress.setString("Waiting");
		album.addWgetChangeListener(albumProgress);

		album.initAlbum();

		if (albumCount >= 3) {
			albumModel.addRow(new Object[2]);
		}
		albumModel.setValueAt(album.getAlbumName(), albumCount, 0);
		albumModel.setValueAt(albumProgress, albumCount, 1);
		albumCount++;

		UIDownload downer = new UIDownload(album, songModel);
		albumQueue.execute(downer);
	}

	private void initComponents() {

		frame = new JFrame();
		frame.setPreferredSize(new Dimension(460, 140));
		frame.setTitle("Magnatune Downloader");
		frame.setResizable(false);
		frame.setIconImage(icon);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.insets = new Insets(2, 2, 2, 2);

		gc.gridx = 0;
		gc.gridy = 0;
		gc.anchor = GridBagConstraints.NORTH;
		gc.gridwidth = GridBagConstraints.REMAINDER;
		gc.fill = GridBagConstraints.BOTH;
		gc.weightx = 1;
		gc.weighty = 0.25;
		albumModel = new MusicModel(3, 2, "Album");
		JTable table = new MusicTable(albumModel);
		DropTarget target = new DropTarget(table, new M3uDropTargetListener());
		target.setDefaultActions(DnDConstants.ACTION_COPY);
		JScrollPane albumPane = new JScrollPane(table);
		// albumPane.setPreferredSize(new Dimension(450, 83));
		albumPane.setBorder(new SoftBevelBorder(BevelBorder.RAISED));
		panel.add(albumPane, gc);

//		gc.anchor = GridBagConstraints.EAST;
//		gc.gridx = 0;
//		gc.gridy = 1;
//		gc.weighty = 0;
//		gc.weightx = 0;
//		gc.fill = GridBagConstraints.NONE;
//		gc.insets = new Insets(5, 2, 5, 2);
//		gc.gridwidth = GridBagConstraints.RELATIVE;
//		newAlbum = new JButton("New Album");
//		panel.add(newAlbum, gc);

		gc.anchor = GridBagConstraints.LINE_END;
		gc.gridx = 2;
		gc.gridy = 1;
		gc.weighty = 0;
		gc.weightx = 0;
		gc.insets = new Insets(5, 2, 5, 2);
		gc.fill = GridBagConstraints.NONE;
		gc.gridwidth = GridBagConstraints.REMAINDER;
		details = new JButton("Details");
		panel.add(details, gc);

		gc.gridx = 0;
		gc.gridy = 2;
		gc.gridwidth = GridBagConstraints.REMAINDER;
		gc.fill = GridBagConstraints.BOTH;
		gc.weightx = 0;
		gc.weighty = 1;
		gc.insets = new Insets(2, 2, 2, 2);
		songModel = new MusicModel(0, 2, "Songs");
		songTable = new MusicTable(songModel);
		((MusicTable) songTable).setRenderer();
		songPane = new JScrollPane(songTable);
		// songPane.setSize(new Dimension(450, 239));
		songPane.setBorder(new SoftBevelBorder(BevelBorder.RAISED));
		panel.add(songPane, gc);

		frame.add(panel);

		details.addActionListener(new ActionListener() {

			boolean isVisible = false;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (isVisible) {
					songPane.setVisible(false);
					int w = frame.getSize().width;
					frame.setSize(new Dimension(w, 152));
				} else {
					songPane.setVisible(true);
					int w = frame.getSize().width;
					frame.setSize(new Dimension(w, 415));
				}
				isVisible = !isVisible;
			}
		});

//		newAlbum.addActionListener(new ActionListener() {
//
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				String message = "Please input Album location";
//				String strUrl = (String) JOptionPane.showInputDialog(frame,
//						message, "New Album", JOptionPane.OK_CANCEL_OPTION,
//						null, null, null);
//				try {
//					addAlbum(strUrl + "hifi.m3u");
//				} catch (MalformedURLException e) {
//					log.error("Url :" + e.getMessage());
//				} catch (M3uException e) {
//					log.error("M3u : " + e.getMessage());
//				}
//			}
//		});

		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {

				List<Runnable> notRun = albumQueue.shutdownNow();
				for (Runnable runner : notRun) {
					log.info("Did not run: " + (UIDownload) runner);
				}
				if (!albumQueue.isShutdown()) {
					log.info("albumQueue not cleanly shutdown.");
				}
			}
		});
	}

	private void loadIcon() {

		URL url = this.getClass().getResource(ICON_LOCATION);
		try {
			icon = ImageIO.read(url);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private class M3uDropTargetListener implements DropTargetListener {

		@Override
		public void dragEnter(DropTargetDragEvent arg0) {

		}

		@Override
		public void dragExit(DropTargetEvent arg0) {
		}

		@Override
		public void dragOver(DropTargetDragEvent arg0) {
		}

		@Override
		public void drop(DropTargetDropEvent event) {

			boolean success = true;
			event.acceptDrop(DnDConstants.ACTION_COPY);
			Transferable transferable = event.getTransferable();
			DataFlavor[] flavors = transferable.getTransferDataFlavors();
			for (DataFlavor flavor : flavors) {
				if (flavor == DataFlavor.stringFlavor) {
					
					try {

						String strUrl = (String) transferable
								.getTransferData(flavor);
						addAlbum(strUrl + "/hifi.m3u");

					} catch (UnsupportedFlavorException e) {
						success = false;
					} catch (IOException e) {
						success = false;
					} catch (M3uException e) {
						log.error("M3u : " + e.getMessage());
						success = false;
					}
				}
			}
			event.dropComplete(success);
		}

		@Override
		public void dropActionChanged(DropTargetDragEvent arg0) {
		}
	}
}
