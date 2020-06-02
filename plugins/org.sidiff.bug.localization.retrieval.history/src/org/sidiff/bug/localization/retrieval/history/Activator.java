package org.sidiff.bug.localization.retrieval.history;

import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;
	
	private static Logger logger;

	static BundleContext getContext() {
		return context;
	}
	
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		Activator.logger = Logger.getLogger(bundleContext.getBundle().getSymbolicName());
	}

	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
		Activator.logger = null;
	}

	public static Logger getLogger() {
		return logger;
	}

}
