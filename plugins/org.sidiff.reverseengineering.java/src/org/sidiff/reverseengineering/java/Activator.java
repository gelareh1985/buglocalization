package org.sidiff.reverseengineering.java;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.sidiff.reverseengineering.uml"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	private static Logger logger;

	public static Logger getLogger() {
		return logger;
	}
	
	public static boolean isLoggable(Level level) {
		return logger.isLoggable(level);
	}
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		Activator.logger = Logger.getLogger(context.getBundle().getSymbolicName());
		
		Level logLevel = Level.WARNING;
		Activator.getLogger().setLevel(logLevel);
		ConsoleHandler consolHandler = new ConsoleHandler();
		consolHandler.setLevel(logLevel);
		Activator.getLogger().addHandler(consolHandler);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
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

}
