package eu.artist.migration.mdt.javaee.java.uml.provider;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.URI;
import org.eclipse.m2m.atl.core.ATLCoreException;
import org.eclipse.m2m.atl.core.IModel;
import org.eclipse.m2m.atl.core.IReferenceModel;
import org.eclipse.m2m.atl.core.emf.EMFInjector;
import org.eclipse.m2m.atl.core.emf.EMFModel;
import org.eclipse.m2m.atl.core.emf.EMFModelFactory;

public class Java2UMLByFileProvider extends Java2UMLProvider<IFile> {

	public Java2UMLByFileProvider(String view) {
		super(view);
	}

	@Override
	public EMFModel getInputModel(IFile source) throws ATLCoreException {
		EMFModelFactory modelFactory = new EMFModelFactory();
		EMFInjector injector = new EMFInjector();
		
		IReferenceModel javaMetamodel = modelFactory.newReferenceModel();
		injector.inject(javaMetamodel, Java2UMLProvider.JAVA_MM_URI);
		
		IModel javaModel = modelFactory.newModel(javaMetamodel);
		injector.inject(javaModel, source.getLocationURI().toString());
		return (EMFModel) javaModel;
	}
	
	@Override
	public URI getOutputURI(IFile source) {
		URI uri = URI.createURI(source.getLocationURI().toString());
		return super.getOutputURI(uri);
	}
}
