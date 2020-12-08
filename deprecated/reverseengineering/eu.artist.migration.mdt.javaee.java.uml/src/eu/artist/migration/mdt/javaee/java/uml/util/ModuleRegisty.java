package eu.artist.migration.mdt.javaee.java.uml.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.m2m.atl.engine.emfvm.ASM;
import org.eclipse.m2m.atl.engine.emfvm.ASMXMLReader;

public class ModuleRegisty {

	private static Map<URL, ASM> modules = new HashMap<>();
	
	public static ASM getModule(URL url) throws IOException {
		ASM module = modules.get(url);
		
		if (module == null) {
			module = loadModule(url.openStream());
			modules.put(url, module);
		}
		
		return module;
	}
	
	public void clear() {
		ModuleRegisty.modules = new HashMap<>();
	}
	
	public static ASM loadModule(InputStream inputStream) {
		return new ASMXMLReader().read(inputStream);
	}
}
