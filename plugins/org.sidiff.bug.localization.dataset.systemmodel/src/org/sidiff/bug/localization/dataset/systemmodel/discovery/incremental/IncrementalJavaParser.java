package org.sidiff.bug.localization.dataset.systemmodel.discovery.incremental;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class IncrementalJavaParser {

	private Map<String, CompilationUnit> parsedCompilationUnits;
	
	private boolean ignoreMethodBodies;
	
	public IncrementalJavaParser(boolean ignoreMethodBodies) {
		this.parsedCompilationUnits = new HashMap<>();
		this.ignoreMethodBodies = ignoreMethodBodies;
	}
	
	public void reset() {
		for (Iterator<Entry<String, CompilationUnit>> iterator = parsedCompilationUnits.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, CompilationUnit> parsedCompilationUnit = iterator.next();
			parsedCompilationUnit.getValue().delete();
			parsedCompilationUnit.setValue(null);
			iterator.remove();
		}
		this.parsedCompilationUnits = new HashMap<>();
	}
	
	public void update(Set<IPath> changed) {
		for (IPath path : changed) {
			String key = getKey(path);
			CompilationUnit compilationUnit = parsedCompilationUnits.get(key);
			
			if (compilationUnit != null) {
				compilationUnit.delete();
				parsedCompilationUnits.put(key, null);
				parsedCompilationUnits.remove(key);
			}
		}
	}
	
	public void update(List<String> removedProjects) {
		if (!removedProjects.isEmpty()) {
			for (Iterator<Entry<String, CompilationUnit>> iterator = parsedCompilationUnits.entrySet().iterator(); iterator.hasNext();) {
				Entry<String, CompilationUnit> parsedCompilationUnit = iterator.next();
				
				for (String removedProject : removedProjects) {
					if (parsedCompilationUnit.getKey().startsWith("/" + removedProject)) {
						parsedCompilationUnit.getValue().delete();
						parsedCompilationUnit.setValue(null);
						iterator.remove();
					}
				}
			}
		}
	}
	
	public CompilationUnit getCompilationUnit(ITypeRoot source) {
		CompilationUnit parsedCompilationUnit = parsedCompilationUnits.get(getKey(source));
		
		if (parsedCompilationUnit == null) {
			parsedCompilationUnit = internal_parseCompilationUnit(source);
			parsedCompilationUnits.put(getKey(source), parsedCompilationUnit);
		}
		
		return parsedCompilationUnit;
	}
	
	protected String getKey(ITypeRoot source) {
		return getKey(source.getPath());
	}
	
	protected String getKey(IPath path) {
		String key = path.toString();
		
		if (key.startsWith("/")) {
			return key;
		} else {
			return "/" + key;
		}
	}
	
	public boolean isIgnoreMethodBodies() {
		return ignoreMethodBodies;
	}

	public void setIgnoreMethodBodies(boolean deepAnalysis) {
		this.ignoreMethodBodies = deepAnalysis;
	}
	
	// >>> protected static method parseCompilationUnit cannot be overwritten :(
	// NOTE: MoDisco is able to resolve binding internally by qualified names.
	// TODO: We might check weather setResolveBindings(true) is needed at all:
	//       - It seems that the packages are not matched correctly (duplicated per class) without resolving binding!

	@SuppressWarnings("deprecation")
	protected CompilationUnit internal_parseCompilationUnit(final ITypeRoot source) {
		// Code parsing : here is indicated the version of JDK (~JLS) to
		// consider, see Class comments
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setResolveBindings(true);
		parser.setSource(source);
		
		// >>> optimization: already ignore method bodies on parsing
		parser.setIgnoreMethodBodies(isIgnoreMethodBodies());
		// <<<
		
		CompilationUnit parsedCompilationUnit = (CompilationUnit) parser.createAST(null);
		return parsedCompilationUnit;
	}

	// <<<
}
