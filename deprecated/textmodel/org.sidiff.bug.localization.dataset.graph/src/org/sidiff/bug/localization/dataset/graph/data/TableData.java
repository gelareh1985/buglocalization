package org.sidiff.bug.localization.dataset.graph.data;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TableData {
	
	private String entrySeperator = "\n";
	
	private String valueSeperator = "\t";
	
	private String commentSeperator = valueSeperator + "# ";
	
	private TextPostProcessor textPostProcessor;

	private List<TableDataEntry> entries;
	
	private int entrySize = 2;
	
	public TableData(int entrySize) {
		this.entrySize = entrySize;
		this.textPostProcessor = new TextPostProcessor();
	}

	public String getEntrySeperator() {
		return entrySeperator;
	}

	public void setEntrySeperator(String entrySeperator) {
		this.entrySeperator = entrySeperator;
	}
	
	public String getValueSeperator() {
		return valueSeperator;
	}

	public void setValueSeperator(String valueSeperator) {
		this.valueSeperator = valueSeperator;
	}
	
	public String getCommentSeperator() {
		return commentSeperator;
	}

	public void setCommentSeperator(String commentSeperator) {
		this.commentSeperator = commentSeperator;
	}

	public TextPostProcessor getTextPostProcessor() {
		return textPostProcessor;
	}

	public void setTextPostProcessor(TextPostProcessor textPostProcessor) {
		this.textPostProcessor = textPostProcessor;
	}

	public List<TableDataEntry> getEntries() {
		
		if (entries == null) {
			this.entries = new ArrayList<>();
		}
		
		return entries;
	}
	
	public TableDataEntry addEntry() {
		TableDataEntry entry = createEnty();
		getEntries().add(entry);
		return entry;
	}
	
	protected TableDataEntry createEnty() {
		return new TableDataEntry(entrySize);
	}

	public int getEntrySize() {
		return entrySize;
	}
	
	public void save(Path path) throws IOException, FileNotFoundException {
		try (OutputStreamWriter outputStream = new OutputStreamWriter(new FileOutputStream(path.toFile()))) {
			for (Iterator<TableDataEntry> iterator = getEntries().iterator(); iterator.hasNext();) {
				TableDataEntry entry = iterator.next();
				entry.write(outputStream, textPostProcessor, valueSeperator, commentSeperator);
				
				if (iterator.hasNext()) {
					outputStream.write(entrySeperator);
				}
			}
		}
	}
}
