package org.sidiff.bug.localization.dataset.systemmodel.discovery.discoverer;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.m2m.atl.core.ATLCoreException;
import org.eclipse.m2m.atl.core.IModel;
import org.eclipse.m2m.atl.core.IReferenceModel;
import org.eclipse.m2m.atl.core.emf.EMFInjector;
import org.eclipse.m2m.atl.core.emf.EMFModel;
import org.eclipse.m2m.atl.core.emf.EMFModelFactory;
import org.eclipse.m2m.atl.core.launch.ILauncher;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;

public class JavaModel2SystemModelProvider {

	private Resource javaSystemModelResource;
	
	public JavaModel2SystemModelProvider(Resource javaSystemModelResource) {
		this.javaSystemModelResource = javaSystemModelResource;
	}
	
	public EMFModel initializeModels(ILauncher transformationLauncher) throws IOException {
		EMFModel systemModel = getOutputSystemModel();
		
		transformationLauncher.addInModel(getInputJavaSystemModel(), "IN1", "JavaSystemModel");
		transformationLauncher.addOutModel(systemModel, "OUT1", "UMLSystemModel");
		
		return systemModel;
	}
	
	public void loadOptions(HashMap<String, Object> options) {
		
		// Inter-model references: change (system model) -> location (uml model)
		options.put("allowInterModelReferences", true);
	}
	
	protected EMFModel getInputJavaSystemModel() {
		EMFModelFactory modelFactory = new EMFModelFactory();
		EMFInjector injector = new EMFInjector();
		
		IReferenceModel systemModelMetamodel = modelFactory.newReferenceModel();
		
		try {
			injector.inject(systemModelMetamodel, SystemModel.NS_URI);
		} catch (ATLCoreException e) {
			e.printStackTrace();
		}
		
		IModel javaSystemModel = modelFactory.newModel(systemModelMetamodel);
		injector.inject(javaSystemModel, javaSystemModelResource);
		
		return (EMFModel) javaSystemModel;
	}
	
	protected EMFModel getOutputSystemModel() {
		EMFModelFactory modelFactory = new EMFModelFactory();
		EMFInjector injector = new EMFInjector();
		
		IReferenceModel systemModelMetamodel = modelFactory.newReferenceModel();
		
		try {
			injector.inject(systemModelMetamodel, SystemModel.NS_URI);
		} catch (ATLCoreException e) {
			e.printStackTrace();
		}
		
		IModel systemModel = modelFactory.newModel(systemModelMetamodel);
		return (EMFModel) systemModel;
	}

	public void loadModules(List<InputStream> modules) throws IOException {
		modules.add(JavaModel2UMLClassSystemModelDiscoverer.class.getResource("/resources/javaChanges2UMLChanges.asm").openStream());
	}
}
