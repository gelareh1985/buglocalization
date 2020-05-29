package org.sidiff.bug.localization.model2adjlist.format;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.stream.Stream;

import org.sidiff.bug.localization.model2adjlist.converter.Object2StringMapper;

/**
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
 * Adjacency List: The adjacency list format consists of lines with node labels.
 * The first label in a line is the source node. Further labels in the line are
 * considered target nodes and are added to the graph along with an edge between
 * the source node and target node.
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
public class AdjacencyList {

	private static final String ENTRY_SEPARATOR = "\n";

	private static final String ELEMENT_SEPARATOR = " ";

	private static final String COMMENT_SEPARATOR = "#";

	private List<String> adjlist;

	private Object2StringMapper object2StringMapperImpl;

	public AdjacencyList(Object2StringMapper object2StringMapperImpl) {
		this.object2StringMapperImpl = object2StringMapperImpl;
		this.adjlist = new ArrayList<>();
	}
	
	public AdjacencyList(File file, Object2StringMapper object2StringMapperImpl) {
		this(object2StringMapperImpl);
		
		try (Scanner scanner = new Scanner(new FileInputStream(file))) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				
				if (!line.isEmpty()) {
					adjlist.add(scanner.nextLine());
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param source  The source node.
	 * @param adjacent All adjacent target nodes.
	 * @param comment A comment for the entry (or <code>null</code>).
	 */
	public void add(Object source, Iterable<?> adjacent, String comment) {
		StringBuilder newEntry = new StringBuilder();

		newEntry.append(object2StringMapperImpl.getString(source));
		newEntry.append(ELEMENT_SEPARATOR);

		for (Object target : adjacent) {
			newEntry.append(object2StringMapperImpl.getString(target));
			newEntry.append(ELEMENT_SEPARATOR);
		}

		if (comment != null) {
			newEntry.append(COMMENT_SEPARATOR);
			newEntry.append(ELEMENT_SEPARATOR);
			newEntry.append(comment);
		}
		
		adjlist.add(newEntry.toString());
	}

	/**
	 * @param source  The source node.
	 * @param adjacent All adjacent target nodes.
	 */
	public void add(Object source, Iterable<?> adjacent) {
		add(source, adjacent, null);
	}

	/**
	 * @param source The source node.
	 * @return All adjacent target nodes.
	 */
	public Iterable<Object> getAdjacent(Object source) {
		String[] targets = getElements(getEntry(object2StringMapperImpl.getString(source)));
		return () -> Arrays.stream(targets, 1, targets.length).map(object2StringMapperImpl::getObject).iterator();
	}
	
	/**
	 * @param source The source node.
	 * @return All adjacent target nodes.
	 */
	public Iterable<Object[]> getEntries() {
		return () -> adjlist.stream().map(e -> Stream.of(getElements(e)).map(object2StringMapperImpl::getObject).toArray()).iterator();
	}
	
	private static String[] getElements(String entry) {
		return entry.substring(0, entry.indexOf(COMMENT_SEPARATOR)).split(ELEMENT_SEPARATOR);
	}
	
	/**
	 * @return The number of entries.
	 */
	public int size() {
		return adjlist.size();
	}

	protected String getFirst() {
		if (!adjlist.isEmpty()) {
			return adjlist.get(0);
		} else {
			throw new NoSuchElementException("list is empty");
		}
	}

	protected String getEntry(String source) {

		for (String entry : adjlist) {
			if (isSourceEntry(source, entry)) {
				return entry;
			}
		}

		throw new NoSuchElementException(source);
	}

	private boolean isSourceEntry(String source, String entry) {
		return source.startsWith(source)
				&& source.regionMatches(source.length(), ELEMENT_SEPARATOR, 0, ELEMENT_SEPARATOR.length());
	}

	protected String getLast() {
		if (!adjlist.isEmpty()) {
			return adjlist.get(adjlist.size() - 1);
		} else {
			throw new NoSuchElementException("list is empty");
		}
	}
	
	@Override
	public String toString() {
		StringBuilder adjlistString = new StringBuilder();
		
		for (String stringBuilder : adjlist) {
			adjlistString.append(stringBuilder);
			adjlistString.append(ENTRY_SEPARATOR);
		}
		
		return adjlistString.substring(0, adjlistString.length() - ENTRY_SEPARATOR.length());
	}
}
