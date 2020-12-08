package eu.artist.migration.mdt.javaee.java.uml.discovery.launcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.m2m.atl.core.service.LauncherService;
import org.eclipse.m2m.atl.engine.emfvm.ASM;
import org.eclipse.m2m.atl.engine.emfvm.adapter.IModelAdapter;
import org.eclipse.m2m.atl.engine.emfvm.launch.EMFVMLauncher;
import org.eclipse.m2m.atl.engine.emfvm.launch.ITool;

public class UMLLauncher extends EMFVMLauncher {

	@Override
	protected Object internalLaunch(ITool[] tools, IProgressMonitor monitor, Map<String, Object> options, Object... modules) {
		List<ASM> superimpose = new ArrayList<ASM>();
		ASM mainModule = getASMFromObject(modules[0]);
		
		for (int i = 1; i < modules.length; i++) {
			superimpose.add(getASMFromObject(modules[i]));
		}
		
		IModelAdapter modelAdapter = new UMLModelAdapter();
		modelAdapter.setAllowInterModelReferences(LauncherService.getBooleanOption(options.get("allowInterModelReferences"), false));	
		return mainModule.run(tools, models, libraries, superimpose, options, monitor, modelAdapter);
	}

}
