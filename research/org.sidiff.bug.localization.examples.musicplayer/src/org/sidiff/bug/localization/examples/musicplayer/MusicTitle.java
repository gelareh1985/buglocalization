package org.sidiff.bug.localization.examples.musicplayer;

import java.util.List;

import javax.sound.sampled.AudioInputStream;

public interface MusicTitle {

	String getName();
	
	List<String> getArtists();
	
	AudioInputStream getMusic();
}
