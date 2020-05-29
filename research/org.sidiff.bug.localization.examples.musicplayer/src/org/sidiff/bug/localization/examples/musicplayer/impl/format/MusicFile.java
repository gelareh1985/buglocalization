package org.sidiff.bug.localization.examples.musicplayer.impl.format;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.sidiff.bug.localization.examples.musicplayer.MusicTitle;

public abstract class MusicFile implements MusicTitle {

	private File file;
	
	private List<String> artists;
	
	private String name;

	public MusicFile(String path) {
		if (path.toLowerCase().endsWith("." + getFileExtension().toLowerCase())) {
			this.file = new File(path);
			
			String fileName = file.getName();
			String[] artistsAndTitle = fileName.split("-" , 2);
			
			if (artistsAndTitle.length == 2) {
				this.name = artistsAndTitle[1];
				this.artists = Arrays.asList(artistsAndTitle[0].split(","));
			} else {
				this.name = fileName;
				this.artists = Collections.emptyList();
			}
		}
	}
	
	public abstract String getFileExtension();
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<String> getArtists() {
		return artists;
	}

	@Override
	public AudioInputStream getMusic() {
		try {
			if ((file != null) && file.exists()) {
				return AudioSystem.getAudioInputStream(file);
			}
		} catch (UnsupportedAudioFileException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
