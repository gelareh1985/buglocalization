package org.sidiff.bug.localization.diagram;

import java.util.logging.Logger;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.sidiff.bug.localization.common.utilities.logging.LoggerUtil;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.sidiff.bug.localization.diagram"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	private static BundleContext context;
	
	private static Logger logger;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		Activator.context = context;
		Activator.logger = LoggerUtil.getLogger(Activator.context);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		Activator.context = null;
		Activator.logger = null;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	public static Logger getLogger() {
		return logger;
	}

}
