package eu.artist.migration.mdt.javaee.java.uml.traces;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Artifact;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Dependency;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.PackageableElement;

public class Model2CodeTrace {
	
	/**
	 * Model Element -> Java File
	 */
	private Map<EObject, String> trace;
	
	/**
	 * Class Name -> Java File
	 */
	private Map<String, String> nameTrace;

	public Model2CodeTrace(Resource classDiagram) {
		this.trace = new LinkedHashMap<>();
		this.nameTrace = new LinkedHashMap<>();
		
		Model rootModel = (Model) classDiagram.getContents().get(0);
		Model sourcesReferencesModel = (Model) rootModel.getPackagedElement("sourcesReferences");
		
		for (PackageableElement element : sourcesReferencesModel.getPackagedElements()) {
			if (element instanceof Dependency) {
				Dependency dependency = (Dependency) element;
				String fileName = getFileName(dependency);
				
				if (!dependency.getClients().isEmpty()) {
					trace.put(dependency.getClients().get(0), getFileName(dependency));
				}
				
				if (fileName != null) {
					nameTrace.put(getClassifierName(fileName), fileName);
				}
			}
		}
	}

	private String getFileName(Dependency dependency) {
		if (dependency.getSuppliers().get(0) instanceof Artifact) {
			if (!dependency.getSuppliers().isEmpty()) {
				return ((Artifact) dependency.getSuppliers().get(0)).getFileName();
			}
		}
		return null;
	}

	private String getClassifierName(String fileName) {
		return fileName.substring(fileName.lastIndexOf("/") + 1, fileName.lastIndexOf("."));
	}
	
	public String getJavaFile(EObject element) {
		return getJavaFile(element, false);
	}
	
	public String getJavaFile(EObject element, boolean byName) {
		String javaFile = trace.get(element);
		EObject container = element.eContainer();
		
		// Fallback: search for container element:
		while ((javaFile == null) && (container != null)) {
			javaFile = trace.get(container);
			container = container.eContainer();
		}
		
		// Fallback: search by class name:
		if (byName) {
			return getJavaFileByName(element);
		}
		
		return javaFile;
	}
	
	public String getJavaFileByName(EObject element) {
		String javaFile = null;
		Classifier containingClassifier = (element instanceof Classifier) ? (Classifier) element : null;
		
		if (containingClassifier != null) {
			javaFile = nameTrace.get(containingClassifier.getName());
		}
		
		EObject container = element.eContainer();
		
		// Fallback: search for container element:
		while ((javaFile == null) && (container != null)) {
			containingClassifier = (container instanceof Classifier) ? (Classifier) container : null;
			
			if (containingClassifier != null) {
				javaFile = nameTrace.get(containingClassifier.getName());
			}
			
			container = container.eContainer();
		}
		
		return javaFile;
	}
	
}
