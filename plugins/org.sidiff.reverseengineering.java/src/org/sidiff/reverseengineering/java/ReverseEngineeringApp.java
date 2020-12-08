package org.sidiff.reverseengineering.java;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

import org.eclipse.emf.common.util.URI;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.sidiff.reverseengineering.java.configuration.TransformationModule;
import org.sidiff.reverseengineering.java.configuration.uml.TransformationDomainUML;
import org.sidiff.reverseengineering.java.configuration.uml.TransformationModuleUML;
import org.sidiff.reverseengineering.java.configuration.uml.TransformationSettingsUML;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Incremental reverse engineering of Java ASTs to EMF models. We compute one
 * model per Java source file which might be regenerated independently. The
 * resolution of references between model is done by utilizing Java name binding
 * as object XMI IDs. Only if no Java name binding is available for a model
 * element it will use EMF's default UUIDs. References to external libraries
 * (not included in (configured) workspace) are collected in a common library
 * model. UUIDs (none Java bindings) are matched and reused, therefore, we use
 * EMF compare to compute a matching to the old model.
 * 
 * @author Manuel Ohrndorf
 */
public class ReverseEngineeringApp implements IApplication {

	// https://www.vogella.com/tutorials/EclipseJDT/article.html
	
	@Override
	public Object start(IApplicationContext context) throws Exception {
		
		// Logging:
		Level logLevel = Level.FINE;
		Activator.getLogger().setLevel(logLevel);
		ConsoleHandler consolHandler = new ConsoleHandler();
		consolHandler.setLevel(logLevel);
		Activator.getLogger().addHandler(consolHandler);

		// Application Settings:
		TransformationSettingsUML settings = new TransformationSettingsUML();
		settings.setBaseURI(URI.createFileURI("C:\\Users\\manue\\git\\repairvision\\plugins.research\\org.sidiff.reverseengineering.java\\test"));
		settings.setIncludeMethodBodies(false);
		settings.setModelFileExtension(TransformationDomainUML.getModelFileExtension());

		TransformationModule transformationModule = new TransformationModuleUML(settings);
		Injector injector = Guice.createInjector(transformationModule);
		IncrementalReverseEngineering engine = injector.getInstance(IncrementalReverseEngineering.class);
		
		Set<String> workspaceProjectsFilter = new HashSet<>(Arrays.asList(new String[] { "" }));
		List<WorkspaceUpdate> workspaceUpdate = WorkspaceUpdate.getAllWorkspaceProjects(workspaceProjectsFilter, true);
//		workspaceUpdate = Collections.singletonList(WorkspaceUpdate.getWorkspaceProject("Test", true));
//		workspaceProjects.addAll(WorkspaceUpdate.getWorkspaceProject("Test"));
		
		engine.performWorkspaceUpdate(workspaceUpdate, WorkspaceUpdate.getProjectScope(workspaceUpdate));
		engine.saveWorkspaceModel();
		engine.saveLibraryModel();

    	System.out.println("FINISHED!");
		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
	}
}
