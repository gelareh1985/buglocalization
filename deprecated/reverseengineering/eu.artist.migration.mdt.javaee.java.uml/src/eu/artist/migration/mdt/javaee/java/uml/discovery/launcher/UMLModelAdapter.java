package eu.artist.migration.mdt.javaee.java.uml.discovery.launcher;

import java.util.Collection;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.m2m.atl.common.ATLLogger;
import org.eclipse.m2m.atl.engine.emfvm.adapter.EMFModelAdapter;
import org.eclipse.m2m.atl.engine.emfvm.lib.AbstractStackFrame;
import org.eclipse.m2m.atl.engine.emfvm.lib.ExecEnv;

public class UMLModelAdapter extends EMFModelAdapter {

	public void set(AbstractStackFrame frame, Object modelElement, String name, Object value) {
		
		// >>> do not use none transformed elements in the target model:
		
		try {
			ExecEnv execEnv = frame.getExecEnv();
			
			if (value instanceof Collection) {
				for (Object obj : (Collection<?>) value) {
					if (!isTargetModelReference(execEnv, obj)) {
						ATLLogger.warning("Only references between target models are allowed for feature '" + name + "'. Element not transformed? "  + obj);
						return;
					}
				}
			} else {
				if (!isTargetModelReference(execEnv, value)) {
					ATLLogger.warning("Only references between target models are allowed for feature '" + name + "'. Element not transformed? "  + value);
					return;
				}
			}
			
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		// <<<
		
		super.set(frame, modelElement, name, value);
	}

	private boolean isTargetModelReference(ExecEnv execEnv, Object obj) {
		if (obj instanceof EObject) {
			if (!execEnv.getModelOf(obj).isTarget()) {
				return false;
			}
		}
		return true;
	}
	
}
