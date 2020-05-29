package org.sidiff.bug.localization.examples.musicplayer.impl.format;

public class WaveMusicTitle extends MusicFile {

	public WaveMusicTitle(String path) {
		super(path);
	}

	@Override
	public String getFileExtension() {
		return "wav";
	}

}
