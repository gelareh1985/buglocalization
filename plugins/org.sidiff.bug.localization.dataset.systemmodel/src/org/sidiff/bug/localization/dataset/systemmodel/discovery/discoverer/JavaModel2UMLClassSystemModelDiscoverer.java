package org.sidiff.bug.localization.dataset.systemmodel.discovery.discoverer;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.m2m.atl.core.emf.EMFModel;
import org.eclipse.m2m.atl.core.launch.ILauncher;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.View;
import org.sidiff.bug.localization.dataset.systemmodel.views.ViewDescriptions;

import eu.artist.migration.mdt.javaee.java.umlclass.discovery.Java2UMLResourceDiscoverer;

public class JavaModel2UMLClassSystemModelDiscoverer extends Java2UMLResourceDiscoverer {
	
	private JavaModel2SystemModelProvider javaModel2SystemModelProvider;
	
	private EMFModel umlSystemModel;

	public JavaModel2UMLClassSystemModelDiscoverer(Resource javaSystemModelResource) {
		this.javaModel2SystemModelProvider = new JavaModel2SystemModelProvider(javaSystemModelResource);
	}
	
	public SystemModel getUmlSystemModel() {
		SystemModel systemModel = (SystemModel) umlSystemModel.getResource().getContents().get(0);
		
		// Set view description:
		View umlView = systemModel.getViewByKind("uml");
		umlView.setName(ViewDescriptions.UML_CLASS_DIAGRAM.getName());
		umlView.setDescription(ViewDescriptions.UML_CLASS_DIAGRAM.getDescription());
		umlView.setKind(ViewDescriptions.UML_CLASS_DIAGRAM.getViewKind());
		
		return systemModel;
	}
	
	@Override
	protected HashMap<String, Object> createOptions() {
		HashMap<String, Object> options = super.createOptions();
		javaModel2SystemModelProvider.loadOptions(options);
		return options;
	}

	protected ILauncher createLauncher() throws IOException {
		ILauncher transformationLauncher = super.createLauncher();
		this.umlSystemModel = javaModel2SystemModelProvider.initializeModels(transformationLauncher);
		return transformationLauncher;
	}
	
	@Override
	protected List<InputStream> loadModules() throws IOException {
		List<InputStream> modules = super.loadModules();
		javaModel2SystemModelProvider.loadModules(modules);
		return modules;
	}
	
}
