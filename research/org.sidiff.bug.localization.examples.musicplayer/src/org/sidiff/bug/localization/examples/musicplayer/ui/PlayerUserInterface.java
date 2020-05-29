package org.sidiff.bug.localization.examples.musicplayer.ui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import org.sidiff.bug.localization.examples.musicplayer.MusicTitle;
import org.sidiff.bug.localization.examples.musicplayer.Player;
import org.sidiff.bug.localization.examples.musicplayer.impl.PlayerImpl;
import org.sidiff.bug.localization.examples.musicplayer.impl.format.MIDIMusicTitle;
import org.sidiff.bug.localization.examples.musicplayer.impl.format.WaveMusicTitle;

public class PlayerUserInterface {

	private JFrame frame;
	
	private JTable table;
	
	private Player player;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PlayerUserInterface window = new PlayerUserInterface(new PlayerImpl());
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public PlayerUserInterface(Player player) {
		this.player = player;
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setTitle("Simple Audio Player");
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Box horizontalBox = Box.createHorizontalBox();
		frame.getContentPane().add(horizontalBox, BorderLayout.SOUTH);
		
		JButton play = new JButton("Play");
		horizontalBox.add(play);
		
		JButton pause = new JButton("Pause");
		pause.addActionListener(event -> player.pause());
		horizontalBox.add(pause);
		
		JButton stop = new JButton("Stop");
		stop.addActionListener(event -> player.stop());
		horizontalBox.add(stop);
		
		JLabel title = new JLabel("");
		horizontalBox.add(title);
		
		table = new JTable();
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setShowHorizontalLines(false);
		frame.getContentPane().add(table, BorderLayout.CENTER);
		
		DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
		tableModel.setColumnIdentifiers(new Object[] {"Titles"});
		
		// Listeners:
		addPlayListener(play, title);
		addStopListener(stop, title);
		addMusicFileDropListener(table);
	}

	private void addPlayListener(JButton play, JLabel title) {
		play.addActionListener(event -> {
			player.play(table.getSelectedRow());
			title.setText(getTitle(player.getCurrentTitle()));
		});
	}
	
	private void addStopListener(JButton stop, JLabel title) {
		stop.addActionListener(event -> title.setText(""));
	}

	private String getTitle(MusicTitle title) {
		if (title != null) {
			return String.join(", ", title.getArtists()) + " - " + title.getName();
		} else {
			return "";
		}
	}

	private void addMusicFileDropListener(JTable table) {
		new DropTarget(table, new DropTargetListener() {
			@Override
			public void drop(DropTargetDropEvent dtde) {
				try {
					Transferable tr = dtde.getTransferable();
					DataFlavor[] flavors = tr.getTransferDataFlavors();
					
					for (int i = 0; i < flavors.length; i++) {
						if (flavors[i].isFlavorJavaFileListType()) {
							dtde.acceptDrop(dtde.getDropAction());
							@SuppressWarnings("unchecked")
							java.util.List<File> files = (java.util.List<File>) tr.getTransferData(flavors[i]);
							
							for (File file : files) {
								if (file.getName().toLowerCase().endsWith(".wav")) {
									player.appendToPlaylist(new WaveMusicTitle(file.getAbsolutePath()));
								} else if (file.getName().toLowerCase().endsWith(".mid")) {
									player.appendToPlaylist(new MIDIMusicTitle(file.getAbsolutePath()));
								}
							}
							dtde.dropComplete(true);
						}
					}
				} catch (Throwable t) {
					t.printStackTrace();
					dtde.rejectDrop();
				}

				updatePlaylist();
			}

			private void updatePlaylist() {
				DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
				
				for (int i = table.getModel().getRowCount(); i < player.getPlaylist().size(); i++) {
					String[] row = {getTitle(player.getPlaylist().get(i))};
					tableModel.addRow(row);
				}
				
				tableModel.fireTableDataChanged();
			}

			@Override
			public void dragEnter(DropTargetDragEvent dtde) {
			}

			@Override
			public void dragOver(DropTargetDragEvent dtde) {
			}

			@Override
			public void dropActionChanged(DropTargetDragEvent dtde) {
			}

			@Override
			public void dragExit(DropTargetEvent dte) {
			}
		});
	}

}
