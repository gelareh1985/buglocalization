package eu.artist.migration.mdt.javaee.java.uml.provider;

import org.eclipse.emf.common.util.URI;
import org.eclipse.m2m.atl.core.ATLCoreException;
import org.eclipse.m2m.atl.core.IModel;
import org.eclipse.m2m.atl.core.IReferenceModel;
import org.eclipse.m2m.atl.core.emf.EMFInjector;
import org.eclipse.m2m.atl.core.emf.EMFModel;
import org.eclipse.m2m.atl.core.emf.EMFModelFactory;

public abstract class Java2UMLProvider<T> implements TransformationProvider<T> {

	public static final String UML_MM_URI = "http://www.eclipse.org/uml2/4.0.0/UML"; //$NON-NLS-1$
	
	public static final String JAVA_MM_URI = "http://www.eclipse.org/MoDisco/Java/0.2.incubation/java"; //$NON-NLS-1$
	
	private String view;
	
	public Java2UMLProvider(String view) {
		this.view = view;
	}

	@Override
	public EMFModel getOutputModel() throws ATLCoreException {
		EMFModelFactory modelFactory = new EMFModelFactory();
		EMFInjector injector = new EMFInjector();
		
		IReferenceModel umlMetamodel = modelFactory.newReferenceModel();
		injector.inject(umlMetamodel, Java2UMLProvider.UML_MM_URI);
		
		IModel umlModel = modelFactory.newModel(umlMetamodel);
		return (EMFModel) umlModel;
	}
	
	public URI getOutputURI(URI inputModelURI) {
		String viewFileExtension = view != null ? view + ".uml" : "uml";
		
		if (inputModelURI != null) {
			return inputModelURI.trimFileExtension().appendFileExtension(viewFileExtension);
		} else {
			return URI.createURI("unknown" + viewFileExtension);
		}
	}
}
