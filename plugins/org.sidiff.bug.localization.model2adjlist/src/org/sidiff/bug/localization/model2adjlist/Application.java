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
import org.eclipse.swt.widgets.Shell;
import org.sidiff.bug.localization.model2adjlist.converter.Model2AdjListConverter;
import org.sidiff.bug.localization.model2adjlist.converter.Object2StringConverter;
import org.sidiff.bug.localization.model2adjlist.converter.Object2StringMapper;
import org.sidiff.bug.localization.model2adjlist.converter.impl.Model2AdjListConverterImpl;
import org.sidiff.bug.localization.model2adjlist.converter.impl.Object2StringConverterImpl;
import org.sidiff.bug.localization.model2adjlist.converter.impl.Object2StringMapperImpl;
import org.sidiff.bug.localization.model2adjlist.format.AdjacencyList;

public class Application implements IApplication {

	@Override
	public Object start(IApplicationContext context) throws Exception {
		
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setSize(0, 0);
	    shell.open();
		
		FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		dialog.setFilterExtensions(new String [] {"*.uml"});
		String file = dialog.open();
		
		if (new File(file).exists()) {
			URI uri = URI.createFileURI(file);
			ResourceSet resourceSet = new ResourceSetImpl();
			Resource resource = resourceSet.getResource(uri, true);
			
			Object2StringConverter obj2String = new Object2StringConverterImpl();
			Object2StringMapper obj2StringMap = new Object2StringMapperImpl(obj2String);
			Model2AdjListConverter model2AdjList = new Model2AdjListConverterImpl(obj2StringMap);
			
			AdjacencyList adjacencyList = model2AdjList.convert(resource.getAllContents());
			System.out.println(adjacencyList);
		} else {
			Activator.getLogger().log(Level.SEVERE, "File not found: " + file);
		}

		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
	}

}
