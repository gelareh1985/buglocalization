package org.sidiff.bug.localization.model2adjlist;

import java.io.File;
import java.util.logging.Level;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.sidiff.bug.localization.model2adjlist.converter.Model2AdjListConverter;
import org.sidiff.bug.localization.model2adjlist.converter.ModelElement2NumberConverter;
import org.sidiff.bug.localization.model2adjlist.converter.impl.Model2AdjListConverterImpl;
import org.sidiff.bug.localization.model2adjlist.converter.impl.ModelElement2NumberConverterImpl;
import org.sidiff.bug.localization.model2adjlist.format.ModelAdjacencyList;
import org.sidiff.bug.localization.model2adjlist.util.Model2AdjacencyListUtil;

public class TestDriverApplication implements IApplication {

	@Override
	public Object start(IApplicationContext context) throws Exception {
		
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setSize(0, 0);
	    shell.open();
		
		FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
		fileDialog.setFilterExtensions(new String [] {"*.uml"});
		String modelFile = fileDialog.open();
		
		if ((modelFile != null) && new File(modelFile).exists()) {
			
			// Load model:
			URI uri = URI.createFileURI(modelFile);
			ResourceSet resourceSet = new ResourceSetImpl();
			Resource resource = resourceSet.getResource(uri, true);
			
			// Convert to adjacency list:
			ModelElement2NumberConverter element2Number = new ModelElement2NumberConverterImpl();
			Model2AdjListConverter model2AdjList = new Model2AdjListConverterImpl(element2Number);
			
			ModelAdjacencyList adjacencyList = model2AdjList.convert(() -> resource.getAllContents());
			
			// Output:
			System.out.println(adjacencyList);
			
			// Save:
			File adjListFile = Model2AdjacencyListUtil.save(modelFile, adjacencyList);
			
			MessageBox outputInfoDialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
			outputInfoDialog.setText("Adjacency List Saved");
			outputInfoDialog.setMessage("Adjacency list saved: " + adjListFile.getAbsolutePath());
			outputInfoDialog.open();
			
			// TEST:
//			ModelAdjacencyList adjacencyListLoaded = Model2AdjacencyListUtil.load(resource, adjListFile, element2Number);
//			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
//			System.out.println(adjacencyListLoaded);
//			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
//			System.out.println(adjacencyListLoaded.getNumber2ModelElementMapper());
//			System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
//			System.out.println(adjacencyListLoaded.getModelElement2NumberMapper());
			
		} else {
			Activator.getLogger().log(Level.SEVERE, "File not found: " + modelFile);
		}

		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
	}

}
