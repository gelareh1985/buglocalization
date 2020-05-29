package org.sidiff.bug.localization.examples.musicplayer.impl;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;

import org.sidiff.bug.localization.examples.musicplayer.MusicTitle;
import org.sidiff.bug.localization.examples.musicplayer.Player;

public class PlayerImpl implements Player{

	private List<MusicTitle> playlist;
	
	private MusicTitle currentTitle;
	
	private Clip clip;
	
	private boolean pause;
	
	private int position;
	
	public PlayerImpl() {
		this.playlist = new LinkedList<>();
		this.currentTitle = null;
		this.pause = false;
	}
	
	@Override
	public void appendToPlaylist(MusicTitle title) {
		playlist.add(title);
	}

	@Override
	public void removeFromPlaylist(MusicTitle title) {
		playlist.remove(title);
	}

	@Override
	public List<MusicTitle> getPlaylist() {
		return playlist;
	}
	
	@Override
	public MusicTitle getCurrentTitle() {
		return currentTitle;
	}
	
	@Override
	public void play(int titleInPlaylist) {
		if (!pause && isPlaying()) {
			stop();
		}
		this.currentTitle = getTitleFromPlaylist(titleInPlaylist);
		this.pause = !startPlayback();
	}
	
	private MusicTitle getTitleFromPlaylist(int titleInPlaylist) {
		if ((titleInPlaylist >= 0) && (titleInPlaylist < getPlaylist().size())) {
			return getPlaylist().get(titleInPlaylist);
		}
		return null;
	}

	private boolean startPlayback() {
		AudioInputStream stream = (currentTitle != null) ? currentTitle.getMusic() : null;
		
		if (stream != null) {
			AudioFormat format = stream.getFormat();
			DataLine.Info info = new DataLine.Info(Clip.class, format);
			
			try {
				this.clip = (Clip) AudioSystem.getLine(info);
				clip.open(stream);
				clip.setFramePosition(position);
		        clip.loop(Clip.LOOP_CONTINUOUSLY);
				return true;
			} catch (LineUnavailableException | IOException e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}

	@Override
	public boolean isPlaying() {
		return (currentTitle != null) && !pause;
	}

	@Override
	public void pause() {
		if (isPlaying()) {
			clip.stop();
			this.position = clip.getFramePosition();
			this.pause = true;
		}
	}

	@Override
	public void stop() {
		stopPlayback();
		this.clip = null;
		this.currentTitle = null;
		this.position = 0;
		this.pause = false;
	}

	private void stopPlayback() {
		if (clip != null) {
			clip.stop();
			clip.close();
		}
	}

}
