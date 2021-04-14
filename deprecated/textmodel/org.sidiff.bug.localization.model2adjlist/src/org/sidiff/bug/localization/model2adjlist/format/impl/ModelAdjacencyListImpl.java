package org.sidiff.bug.localization.model2adjlist.format.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.sidiff.bug.localization.model2adjlist.format.AdjacencyListEntry;
import org.sidiff.bug.localization.model2adjlist.format.ModelAdjacencyList;
import org.sidiff.bug.localization.model2adjlist.trace.ModelElement2NumberMapper;
import org.sidiff.bug.localization.model2adjlist.trace.Number2ModelElementMapper;

public class ModelAdjacencyListImpl implements ModelAdjacencyList {

	private List<AdjacencyListEntry> adjlist;

	private ModelElement2NumberMapper modelElement2Number;
	
	private Number2ModelElementMapper number2ModelElement;

	public ModelAdjacencyListImpl(
			ModelElement2NumberMapper modelElement2Number,
			Number2ModelElementMapper number2ModelElement) {
		
		this.modelElement2Number = modelElement2Number;
		this.number2ModelElement = number2ModelElement;
		this.adjlist = new ArrayList<>();
	}
	
	public ModelAdjacencyListImpl(
			ModelElement2NumberMapper modelElement2Number,
			Number2ModelElementMapper number2ModelElement,
			Resource modelFile, File adjacencyListFile)
			throws FileNotFoundException, IOException {
		
		this(modelElement2Number, number2ModelElement);
		
		try (InputStreamReader reader = new InputStreamReader(new FileInputStream(adjacencyListFile))) {
			read(reader);
		}
		
		for (AdjacencyListEntry entry : adjlist) {
			trace(modelFile, entry);
		}
	}
	
	private void trace(Resource model, AdjacencyListEntry entry) {
		Integer modelElementNumerical = entry.getNode();
		EObject modelElement = model.getEObject(entry.getComment());
		
		modelElement2Number.map(modelElement, modelElementNumerical);
		number2ModelElement.map(modelElementNumerical, modelElement);
	}
	
	@Override
	public void addModelElement(EObject source, Collection<? extends EObject> adjacent) {
		int sourceNumerical = modelElement2Number.getNumber(source);
		int[] adjacentNumerical = adjacent.stream().mapToInt(modelElement2Number::getNumber).toArray();
		String comment = EcoreUtil.getURI(source).fragment().toString();
		
		number2ModelElement.map(sourceNumerical, source);

		AdjacencyListEntry newEntry = new AdjacencyListEntryImpl(sourceNumerical, adjacentNumerical, comment);
		adjlist.add(newEntry);
	}
	
	@Override
	public List<AdjacencyListEntry> getEntries() {
		return adjlist;
	}
	
	@Override
	public void read(InputStreamReader inputStreamReader) throws IOException {
		try (Scanner scanner = new Scanner(inputStreamReader)) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				
				if (!line.isEmpty()) {
					AdjacencyListEntry newEntry = new AdjacencyListEntryImpl(line);
					adjlist.add(newEntry);
				}
			}
		}
	}

	@Override
	public void write(OutputStreamWriter outputStreamWriter) throws IOException {
		for (Iterator<AdjacencyListEntry> iterator = adjlist.iterator(); iterator.hasNext();) {
			AdjacencyListEntry entry = iterator.next();
			entry.write(outputStreamWriter);
			
			if (iterator.hasNext()) {
				outputStreamWriter.write(ENTRY_SEPARATOR);
			}
		}
	}
	
	@Override
	public ModelElement2NumberMapper getModelElement2NumberMapper() {
		return modelElement2Number;
	}
	
	@Override
	public Number2ModelElementMapper getNumber2ModelElementMapper() {
		return number2ModelElement;
	}

	@Override
	public String toString() {
		StringBuilder toString = new StringBuilder();
		
		for (Iterator<AdjacencyListEntry> iterator = adjlist.iterator(); iterator.hasNext();) {
			AdjacencyListEntry entry = iterator.next();
			toString.append(entry);
			
			if (iterator.hasNext()) {
				toString.append(ENTRY_SEPARATOR);
			}
		}
		
		return toString.toString();
	}

}
