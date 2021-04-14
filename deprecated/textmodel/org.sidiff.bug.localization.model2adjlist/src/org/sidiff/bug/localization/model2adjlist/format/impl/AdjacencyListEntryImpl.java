package org.sidiff.bug.localization.model2adjlist.format.impl;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;

import org.sidiff.bug.localization.model2adjlist.format.AdjacencyListEntry;

public class AdjacencyListEntryImpl implements AdjacencyListEntry {

	private int node;
	
	private int[] adjacent;
	
	private String comment;
	
	public AdjacencyListEntryImpl(int node, int[] adjacent, String comment) {
		this.node = node;
		this.adjacent = adjacent;
		this.comment = comment;
	}
	
	public AdjacencyListEntryImpl(String entry) {
		read(entry);
	}

	@Override
	public int getNode() {
		return node;
	}

	@Override
	public int[] getAdjacent() {
		return adjacent;
	}

	@Override
	public String getComment() {
		return comment;
	}

	@Override
	public void read(String entry) {
		this.comment = parseComment(entry);
		int[] elements = parseElements(entry);
		
		if (elements.length > 0 ) {
			this.node = elements[0];
		}
		
		if (elements.length > 1) {
			this.adjacent = Arrays.copyOfRange(elements, 1, elements.length);
		}
	}
	
	private int[] parseElements(String entry) {
		int commentPosition = entry.indexOf(COMMENT_SEPARATOR);
		commentPosition = (commentPosition != -1) ? commentPosition : entry.length();
		
		String[] values = entry.substring(0, commentPosition).split(ELEMENT_SEPARATOR);
		return Arrays.stream(values).mapToInt(Integer::valueOf).toArray();
	}
	
	private String parseComment(String entry) {
		int separator = entry.indexOf(COMMENT_SEPARATOR);
		
		if (separator != -1) {
			return entry.substring(separator + ELEMENT_SEPARATOR.length(), entry.length()).trim();
		} else {
			return null;
		}
	}

	@Override
	public void write(OutputStreamWriter outputStreamWriter) throws IOException {
		outputStreamWriter.write(toString());
	}
	
	@Override
	public String toString() {
		StringBuilder text = new StringBuilder();
		text.append(node);
		text.append(ELEMENT_SEPARATOR);
		
		for (int adjacentNode : adjacent) {
			text.append(adjacentNode);
			text.append(ELEMENT_SEPARATOR);
		}
		
		if ((comment != null) && (!comment.isEmpty())) {
			text.append(COMMENT_SEPARATOR);
			text.append(ELEMENT_SEPARATOR);
			text.append(comment);
		} else {
			text.delete(text.length() - ELEMENT_SEPARATOR.length(), text.length());
		}
		
		return text.toString();
	}

}
