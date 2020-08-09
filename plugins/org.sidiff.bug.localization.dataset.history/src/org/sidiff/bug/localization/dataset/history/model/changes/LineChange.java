package org.sidiff.bug.localization.dataset.history.model.changes;

import java.util.Arrays;

public class LineChange {

	/** Type of edit */
	public enum LineChangeType {
		
		/** Sequence B has inserted the region. */
		INSERT,

		/** Sequence B has removed the region. */
		DELETE,

		/** Sequence B has replaced the region with different content. */
		REPLACE,

		/** Sequence A and B have zero length, describing nothing. */
		EMPTY;
	}
	
	private LineChangeType type;
	
	/**
	 * location = {begin A, end A, begin B, end B} 
	 */
	private int[] location;
	
	public LineChange() {
		this.location = new int[4];
	}

	public LineChangeType getType() {
		return type;
	}

	public void setType(LineChangeType lineChangeType) {
		this.type = lineChangeType;
	}

	public int[] getLocation() {
		return location;
	}

	public void setLocation(int[] location) {
		this.location = location;
	}

	public int getBeginA() {
		return location[0];
	}

	public void setBeginA(int beginA) {
		this.location[0] = beginA;
	}

	public int getEndA() {
		return location[1];
	}

	public void setEndA(int endA) {
		this.location[1] = endA;
	}

	public int getBeginB() {
		return location[2];
	}

	public void setBeginB(int beginB) {
		this.location[2] = beginB;
	}

	public int getEndB() {
		return location[3];
	}

	public void setEndB(int endB) {
		this.location[3] = endB;
	}

	@Override
	public String toString() {
		return "LineChange [type=" + type + ", location=" + Arrays.toString(location) + "]";
	}
}
