package org.sidiff.bug.localization.common.utilities.logging;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.ResourcesPlugin;
import org.osgi.framework.BundleContext;

public class LoggerUtil {

	public static Level DEFAULT_CONSOLE_LEVEL = Level.FINE;
	
	public static Level DEFAULT_FILE_LEVEL = Level.INFO;
	
	public static Level PERFORMANCE = Level.FINER;
	
	private static Logger INSTANCE;
	
	public static Logger getLogger(BundleContext bundleContext) {
		return getLogger(bundleContext.getBundle().getSymbolicName());
	}
	
	public static Logger getLogger(String name) {
		
		if (INSTANCE == null) {
			INSTANCE = Logger.getLogger("org.sidiff.bug.localization");
			INSTANCE.setLevel(LoggerUtil.DEFAULT_CONSOLE_LEVEL);
			INSTANCE.setUseParentHandlers(false);
			
			ConsoleHandler consolHandler = new ConsoleHandler();
			consolHandler.setLevel(LoggerUtil.DEFAULT_CONSOLE_LEVEL);
			INSTANCE.addHandler(consolHandler);
			
			try {
				Path logFilePath = Paths.get(
						ResourcesPlugin.getWorkspace().getRoot().getLocation().toString(),
						"org.sidiff.bug.localization.log.xml");
				Files.deleteIfExists(logFilePath);
				
				FileHandler fileHandler = new FileHandler(logFilePath.toString());
				fileHandler.setLevel(LoggerUtil.DEFAULT_FILE_LEVEL);
				INSTANCE.addHandler(fileHandler);
			} catch (SecurityException | IOException e) {
				e.printStackTrace();
			}
		}
		
		return INSTANCE;
	}
	
}
