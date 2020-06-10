package eu.artist.migration.mdt.javaee.java.uml.provider;

import org.eclipse.emf.common.util.URI;
import org.eclipse.m2m.atl.core.ATLCoreException;
import org.eclipse.m2m.atl.core.emf.EMFModel;

public interface TransformationProvider<T> {

	/**
	 * @param source       The input model source.
	 * @return The input model.
	 * @throws ATLCoreException
	 */
	EMFModel getInputModel(T source) throws ATLCoreException;
	
	/**
	 * @return The input model.
	 * @throws ATLCoreException
	 */
	EMFModel getOutputModel() throws ATLCoreException;

	/**
	 * @param source The input model source.
	 * @return The output location of the transformed model.
	 */
	URI getOutputURI(T source);

}
