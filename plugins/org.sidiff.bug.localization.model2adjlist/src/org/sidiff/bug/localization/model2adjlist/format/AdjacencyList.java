package org.sidiff.bug.localization.model2adjlist.format;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * <h1>
 * Adjacency List Adapter:
 * </h1>
 * 
 * <p>
 * NetworkX is a Python package for the creation, manipulation, and study of the
 * structure, dynamics, and functions of complex networks.
 * </p>
 * 
 * <ul>
 * <li>https://networkx.github.io/documentation/stable/index.html</li>
 * <li>https://networkx.github.io/documentation/stable/reference/readwrite/adjlist.html</li>
 * </ul>
 * 
 * <p>
 * The adjacency list format consists of lines with node labels. The first label
 * in a line is the source node. Further labels in the line are considered
 * target nodes and are added to the graph along with an edge between the source
 * node and target node.
 * </p>
 * 
 * <p>
 * The graph with edges a-b, a-c, d-e can be represented as the following
 * adjacency list (anything following the # in a line is a comment):
 * </p>
 * 
 * <code>
 * <br> a b c # source target target
 * <br> d e
 * </code>
 */
public interface AdjacencyList {
	
	static final String ENTRY_SEPARATOR = "\n";

	/**
	 * @return All entries of the adjacency list.
	 */
	List<AdjacencyListEntry> getEntries();

	/**
	 * @param inputStreamReader The reading source.
	 * @throws IOException 
	 */
	void read(InputStreamReader inputStreamReader) throws IOException;
	
	/**
	 * @param outputStreamWriter The writing target.
	 * @throws IOException 
	 */
	void write(OutputStreamWriter outputStreamWriter) throws IOException;

}