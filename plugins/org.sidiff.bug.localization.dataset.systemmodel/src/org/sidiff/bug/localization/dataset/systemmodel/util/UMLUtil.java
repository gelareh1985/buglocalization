package org.sidiff.bug.localization.dataset.systemmodel.util;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.uml2.common.util.CacheAdapter;

public class UMLUtil {

	/**
	 * @param resources Unloads the given resources to prevent resource leaks.
	 */
	@SuppressWarnings("rawtypes")
	public static void unloadUMLModels(Collection<Resource> resources) {
		// Unload models for garbage collection -> prevent resource leaks:
		try {
			// CacheAdapter/ECrossReferenceAdapter resource leak: remove adapter, clear caches, unload all resources 
			for (Iterator<Resource> iterator = resources.iterator(); iterator.hasNext();) {
				Resource resource = (Resource) iterator.next();
				ECrossReferenceAdapter crossReferenceAdapter = ECrossReferenceAdapter.getCrossReferenceAdapter(resource);

				if (crossReferenceAdapter != null) {
					crossReferenceAdapter.unsetTarget(resource);
				}

				if (CacheAdapter.getInstance() != null) {
					CacheAdapter.getInstance().clear(resource);
				}

				resource.unload();
			}
		} catch (Throwable e) {
			// e.printStackTrace(); // FIXME Index-Out-Of-Bounds on resource.unload();
		}

		// WORKAROUND: Clear resource leaks of UML CacheAdapter:
		try {
			CacheAdapter cacheAdapter = CacheAdapter.getInstance();
			cacheAdapter.clear();

			// Clear inverseCrossReferencer map:
			Field inverseCrossReferencerField = ECrossReferenceAdapter.class.getDeclaredField("inverseCrossReferencer");
			inverseCrossReferencerField.setAccessible(true);
			Object inverseCrossReferencerValue = inverseCrossReferencerField.get(cacheAdapter);

			if (inverseCrossReferencerValue instanceof Map) {
				((Map) inverseCrossReferencerValue).clear();
			}

			// Clear proxyMap map:
			Class<?> inverseCrossReferencerClass = Class.forName("org.eclipse.emf.ecore.util.ECrossReferenceAdapter$InverseCrossReferencer");
			Field proxyMapField = inverseCrossReferencerClass.getDeclaredField("proxyMap");
			proxyMapField.setAccessible(true);
			Object proxyMapValue = proxyMapField.get(inverseCrossReferencerValue);

			if (proxyMapValue instanceof Map) {
				((Map) proxyMapValue).clear();
			}

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
