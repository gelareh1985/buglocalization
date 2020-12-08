package org.sidiff.reverseengineering.java.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Level;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.sidiff.reverseengineering.java.Activator;

/**
 * Some convenient functionality to parse and analyze Java models. 
 * 
 * @author Manuel Ohrndorf
 */
public class JavaParser {

	/**
	 * The AST's Java language specification used for parsing.
	 */
	public static final int JAVA_LANGUAGE_SPECIFICATION = AST.JLS11; // Latest long term support version of Java
																		// (11-2020)

	/**
	 * @param project           The Java project to be parsed.
	 * @param parseMethodBodies <code>true</code> if the method bodies should be
	 *                          parsed in the AST; <code>false</code> to ignore
	 *                          method bodies.
	 * @return The source files mapped to the parsed Java ASTs.
	 * @throws JavaModelException
	 */
	public Map<ICompilationUnit, CompilationUnit> parse(IProject project, Predicate<IResource> filter, boolean parseMethodBodies)
			throws JavaModelException {
		IPackageFragment[] packages = JavaCore.create(project).getPackageFragments();
		List<ICompilationUnit> sources = new ArrayList<>();
		IJavaProject javaProject = null;

		for (IPackageFragment javaPackage : packages) {
			if (javaPackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
				for (ICompilationUnit unit : javaPackage.getCompilationUnits()) {
					if (filter.test(unit.getResource())) {
						sources.add(unit);
					}
				}

				if (javaProject == null) {
					javaProject = javaPackage.getJavaProject();
				}
			}
		}

		if (!sources.isEmpty() && javaProject != null) {
			return parse(javaProject, sources, parseMethodBodies);
		} else {
			return Collections.emptyMap();
		}
	}

	/**
	 * Parses a single Java source file.
	 * 
	 * @param source            The Java source file.
	 * @param parseMethodBodies <code>true</code> if the method bodies should be
	 *                          parsed in the AST; <code>false</code> to ignore
	 *                          method bodies.
	 * @return The source file mapped to the parsed Java AST.
	 */
	public CompilationUnit parse(ICompilationUnit source, boolean parseMethodBodies) {
		ASTParser parser = ASTParser.newParser(JAVA_LANGUAGE_SPECIFICATION);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(source);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true); // generates binding also for none resolvable/missing types
		parser.setIgnoreMethodBodies(parseMethodBodies);
		CompilationUnit ast = (CompilationUnit) parser.createAST(null);

		return ast;
	}

	/**
	 * Parsed multiple Java source files at ones. It shares binding information
	 * which saves a significant amount of storage (~1/4) for larger source sets.
	 * However, garbage collection for single ASTs might not be possible until none
	 * of the ASTs is references anymore.
	 * 
	 * @param project           The source files have to be in the same project.
	 * @param sources           The Java source files.
	 * @param parseMethodBodies <code>true</code> if the method bodies should be
	 *                          parsed in the AST; <code>false</code> to ignore
	 *                          method bodies.
	 * @return The source files mapped to the parsed Java ASTs.
	 */
	public Map<ICompilationUnit, CompilationUnit> parse(
			IJavaProject project, List<ICompilationUnit> sources, boolean parseMethodBodies) {

		try {
			ASTParser parser = ASTParser.newParser(JAVA_LANGUAGE_SPECIFICATION);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setProject(project);
			parser.setResolveBindings(true);
			parser.setBindingsRecovery(true); // generates binding also for none resolvable/missing types
			parser.setIgnoreMethodBodies(!parseMethodBodies);
			
			Map<ICompilationUnit, CompilationUnit> asts = new LinkedHashMap<>();
			
			parser.createASTs(sources.toArray(new ICompilationUnit[0]), new String[0], new ASTRequestor() {
				
				@Override
				public void acceptAST(ICompilationUnit source, CompilationUnit ast) {
					asts.put(source, ast);
				}
			}, null);
			
			return asts;
		} catch (Throwable e) {
			if (Activator.getLogger().isLoggable(Level.WARNING)) {
				Activator.getLogger().log(Level.WARNING, "An exception occurred during the compilation of "
						+ project.getProject().getName() + ". Trying again by single resource compilation...");
			}
			
			// Fallback to single file compilation:
			Map<ICompilationUnit, CompilationUnit> asts = new LinkedHashMap<>();
			
			for (ICompilationUnit source : sources) {
				try {
					CompilationUnit ast = parse(source, parseMethodBodies);
					asts.put(source, ast);
				}  catch (Throwable e2) {
					if (Activator.getLogger().isLoggable(Level.SEVERE)) {
						Activator.getLogger().log(Level.SEVERE, "Could not compile Java resource: " + source.getResource());
					}
				}
			}
			
			return asts;
		}
	}

	/**
	 * @param javaAST A Java ASTs.
	 * @return Binding key to a java AST.
	 */
	public String getASTBindingKey(CompilationUnit javaAST) {

		for (Object type : javaAST.types()) {
			if (type instanceof AbstractTypeDeclaration) {
				return ((AbstractTypeDeclaration) type).resolveBinding().getKey();
			}
		}

		return null;
	}
	
}
