package org.sidiff.bug.localization.dataset.systemmodel.discovery.incremental;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
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
		this.parsedCompilationUnits = new LinkedHashMap<>();
		this.ignoreMethodBodies = ignoreMethodBodies;
	}
	
	public void reset() {
		parsedCompilationUnits = new HashMap<>();
		System.gc();
	}
	
	public void update(Set<IPath> changed) {
		for (IPath path : changed) {
			parsedCompilationUnits.remove(getKey(path));
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
