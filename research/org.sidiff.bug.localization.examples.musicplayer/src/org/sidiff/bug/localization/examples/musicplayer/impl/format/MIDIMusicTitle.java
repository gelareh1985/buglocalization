package org.sidiff.bug.localization.examples.musicplayer.impl.format;

public class MIDIMusicTitle extends MusicFile {

	public MIDIMusicTitle(String path) {
		super(path);
	}

	@Override
	public String getFileExtension() {
		return "mid";
	}

}
