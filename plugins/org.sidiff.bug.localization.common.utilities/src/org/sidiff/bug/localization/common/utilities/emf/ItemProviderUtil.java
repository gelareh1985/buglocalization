package org.sidiff.bug.localization.common.utilities.emf;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.EMFEditPlugin;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.emf.edit.ui.provider.ExtendedImageRegistry;

/**
 * Retrieve labels and images of EMF items.
 * 
 * @author Manuel Ohrndorf
 */
public class ItemProviderUtil {

	protected static AdapterFactoryLabelProvider emfLabelProvider;
	
	static {
		// EMF Adapter (Item-Provider) Registry:
		ComposedAdapterFactory adapterFactory = new ComposedAdapterFactory(
				ComposedAdapterFactory.Descriptor.Registry.INSTANCE);

		// Display model resources:
		adapterFactory.addAdapterFactory(new ResourceItemProviderAdapterFactory());

		// If the model is not in the registry then display it as in EMF-Reflective-Editor:
		adapterFactory.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());
		
		emfLabelProvider = new AdapterFactoryLabelProvider(adapterFactory);
	}
	/**
	 * Fetches the label image specific to this object instance.
	 * 
	 * @param object
	 *            The object to label.
	 * @return The label image specific to this object instance or <code>null</code>.
	 */
	public static Object getImageByObject(Object object) {
		return emfLabelProvider.getImage(object);
	}
	
	/**
	 * Fetches the label image specific to the given type.
	 * 
	 * @param type
	 *            The type.
	 * @return The label image specific to the given type or <code>null</code>.
	 */
	public static Object getImageByType(EClass type) {
		if (type != null) {
			try {
				EObject tmp = type.getEPackage().getEFactoryInstance().create(type);
				return getImageByObject(tmp);
			} catch (Exception e) {
			}
			
			try {
				return ExtendedImageRegistry.INSTANCE.getImage(EMFEditPlugin.INSTANCE.getImage("/full/obj16/Item"));
			} catch (Exception e) {
			}
		}
		return null;
	}
	
	/**
	 * Fetches the label text specific to this object instance.
	 * 
	 * @param object
	 *            The object to label.
	 * @return The label text specific to this object instance.
	 */
	public static String getTextByObject(Object object) {
		
		if (object != null) {
			return emfLabelProvider.getText(object);
		} else {
			return "null";
		}
	}
}
