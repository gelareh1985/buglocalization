package org.sidiff.bug.localization.model2adjlist.format;

import java.io.IOException;
import java.io.OutputStreamWriter;

public interface AdjacencyListEntry {

	static final String ELEMENT_SEPARATOR = " ";

	static final String COMMENT_SEPARATOR = "#";
	
	/**
	 * @return The node that specifies its adjacent nodes.
	 */
	int getNode();

	/**
	 * @return The adjacent nodes.
	 */
	int[] getAdjacent();

	/**
	 * @return A annotation added to entry.
	 */
	String getComment();
	
	/**
	 * @param entry The entry as a string.
	 */
	void read(String entry);
	
	/**
	 * @param outputStreamWriter The writing target.
	 * @throws IOException 
	 */
	void write(OutputStreamWriter outputStreamWriter) throws IOException;
}
