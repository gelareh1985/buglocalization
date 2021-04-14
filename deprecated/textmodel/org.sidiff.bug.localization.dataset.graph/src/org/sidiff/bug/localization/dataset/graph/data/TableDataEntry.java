package org.sidiff.bug.localization.dataset.graph.data;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class TableDataEntry {
	
	private Object[] values;
	
	private String comment;
	
	public TableDataEntry(int size) {
		this.values = new Object[size];
	}

	public Object[] getValues() {
		return values;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void write(
			OutputStreamWriter outputStream, TextPostProcessor textPostProcessor, 
			String valueSeperator, String commentSeperator) throws IOException {
		
		for (int i = 0; i < values.length; i++) {
			Object value = values[i];
			
			if (value instanceof String) {
				value = textPostProcessor.process((String) value);
			}
			
			outputStream.write(value.toString());
			
			if (i < (values.length - 1)) {
				outputStream.write(valueSeperator);
			}
		}
		
		if (comment != null) {
			outputStream.write(commentSeperator + textPostProcessor.removeLineBreaks(comment));
		}
	}
}
