package org.sidiff.bug.localization.model2adjlist.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.eclipse.emf.ecore.resource.Resource;
import org.sidiff.bug.localization.model2adjlist.converter.ModelElement2NumberConverter;
import org.sidiff.bug.localization.model2adjlist.format.AdjacencyList;
import org.sidiff.bug.localization.model2adjlist.format.ModelAdjacencyList;
import org.sidiff.bug.localization.model2adjlist.format.impl.ModelAdjacencyListImpl;
import org.sidiff.bug.localization.model2adjlist.trace.impl.ModelElement2NumberMapperImpl;
import org.sidiff.bug.localization.model2adjlist.trace.impl.Number2ModelElementMapperImpl;

public class Model2AdjacencyListUtil {

	public static File save(String modelFile, AdjacencyList adjacencyListImpl) throws IOException, FileNotFoundException {
		File adjListFile = new File(modelFile.substring(0, modelFile.lastIndexOf(".")) + ".adjlist");
		save(adjListFile, adjacencyListImpl);
		return adjListFile;
	}
	
	public static void save(File adjListFile, AdjacencyList adjacencyListImpl) throws IOException, FileNotFoundException {
		try (OutputStreamWriter outputStream = new OutputStreamWriter(new FileOutputStream(adjListFile))) {
			adjacencyListImpl.write(outputStream);
		}
	}
	
	public static ModelAdjacencyList load(Resource model, File adjListFile, ModelElement2NumberConverter modelElement2NumberConverter) 
			throws FileNotFoundException, IOException {
		
		ModelAdjacencyList adjacencyList = new ModelAdjacencyListImpl(
				new ModelElement2NumberMapperImpl(modelElement2NumberConverter),
				new Number2ModelElementMapperImpl(),
				model, adjListFile);
		
		return adjacencyList;
	}
}
