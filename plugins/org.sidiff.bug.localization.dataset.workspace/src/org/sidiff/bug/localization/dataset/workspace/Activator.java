package org.sidiff.bug.localization.dataset.workspace;

import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.sidiff.bug.localization.common.utilities.logging.LogLevel;

public class Activator implements BundleActivator {

	private static BundleContext context;
	
	private static Logger logger;

	static BundleContext getContext() {
		return context;
	}
	
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		Activator.logger = Logger.getLogger(bundleContext.getBundle().getSymbolicName());
		Activator.logger.setLevel(LogLevel.DEFAULT_LEVEL);
	}

	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
		Activator.logger = null;
	}

	public static Logger getLogger() {
		return logger;
	}

}
