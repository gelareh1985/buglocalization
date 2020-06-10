package eu.artist.migration.mdt.javaee.java.uml.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.ecore.resource.Resource;

import eu.artist.migration.mdt.javaee.java.uml.provider.Java2UMLProvider;

public class Java2UMLUtil {

	public static boolean isJavaModel(IFile source) {
		if (!source.exists()) {
			return false;
		}
		return source.getName().endsWith(".xmi");
	}
	
	public static boolean isJavaModel(Resource source) {
		if (source.getContents().isEmpty()) {
			return false;
		}
		return source.getContents().get(0).eClass().getEPackage().getNsURI().equals(Java2UMLProvider.JAVA_MM_URI);
	}
	
}
