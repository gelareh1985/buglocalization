package org.sidiff.bug.localization.examples.musicplayer;

import java.util.List;

public interface Player {

	void appendToPlaylist(MusicTitle title);
	
	void removeFromPlaylist(MusicTitle title);
	
	List<MusicTitle> getPlaylist();
	
	MusicTitle getCurrentTitle();
	
	void play(int titleInPlaylist);
	
	boolean isPlaying();
	
	void pause();
	
	void stop();
	
}
