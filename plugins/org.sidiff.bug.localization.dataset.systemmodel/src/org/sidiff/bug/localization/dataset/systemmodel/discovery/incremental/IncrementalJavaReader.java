package org.sidiff.bug.localization.dataset.systemmodel.discovery.incremental;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.modisco.infra.common.core.logging.MoDiscoLogger;
import org.eclipse.modisco.java.discoverer.internal.JavaActivator;
import org.eclipse.modisco.java.discoverer.internal.io.java.JavaReader;
import org.eclipse.modisco.java.emf.JavaFactory;
import org.eclipse.modisco.kdm.source.extension.discovery.ISourceRegionNotifier;

/**
 * Optimization: store and update AST incrementally on changes.
 * 
 * @author Manuel Ohrndorf
 */
@SuppressWarnings({ "restriction", "deprecation" })
public class IncrementalJavaReader extends JavaReader {
	
	private IncrementalJavaParser javaParser;
	
	public IncrementalJavaReader(
			IncrementalJavaParser javaParser,
			JavaFactory factory, Map<String, Object> options,
			ISourceRegionNotifier<?> abstractRegionDiscoverer) {
		
		super(factory, options, abstractRegionDiscoverer);
		this.javaParser = javaParser;
	}
	
	/*
	 * Copied stuff to redirect AST parser creation: parseTypeRoot -> parseCompilationUnit
	 */
	
	@Override
	protected void parseTypeRoot(ITypeRoot source) {
		// >>> protected static method parseCompilationUnit cannot be overwritten :(
		org.eclipse.jdt.core.dom.CompilationUnit parsedCompilationUnit = javaParser.getCompilationUnit(source);
		// <<<
		
		String fileContent = null;
		String filePath = null;
		try {
			if (source instanceof ICompilationUnit) {
				IFile theIFile = ResourcesPlugin.getWorkspace().getRoot()
						.getFile(parsedCompilationUnit.getJavaElement().getPath());
				// getContent(IFile) is faster than ICompilationUnit.getSource()
				fileContent = getContent(theIFile).toString();
				IProject project = source.getJavaProject().getProject();
				filePath = getRelativePath(project, parsedCompilationUnit);
			} else {
				// IJavaElement.CLASS_FILE
				fileContent = getFileContent((IClassFile) source);
				filePath = getPath((IClassFile) source);
			}
			visitCompilationUnit(getResultModel(), parsedCompilationUnit, filePath, fileContent);

		} catch (Exception e) {
			MoDiscoLogger.logError(e, JavaActivator.getDefault());
		}
	}
	
	/// >>> not accessible: org.eclipse.modisco.java.discoverer.internal.io.library.LibraryReader
	
	/**
	 * @see org.eclipse.jdt.core.ISourceReference#getSource()
	 */
	public static String getFileContent(final IClassFile classFile) {
		String source = null;
		try {
			source = classFile.getSource();
		} catch (JavaModelException e) {
			// Nothing
			assert (true); // dummy code for "EmptyBlock" rule
		}
		return source;
	}
	
	/**
	 * Returns the archive-relative path of the class file. If this class file
	 * is in an archive (workspace or external), the path will be the path
	 * inside the archive. If it is in a folder (workspace or external), the
	 * path will be the full absolute path to this class file.
	 *
	 * @param classFile
	 *            the class file
	 * @return the archive-relative path
	 */
	public static String getPath(final IClassFile classFile) {
		IPackageFragmentRoot library = (IPackageFragmentRoot) classFile
				.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);

		String filePath = null;
		if (library.isArchive()) { // zip or jar
			IPackageFragment parent = (IPackageFragment) classFile.getParent();
			String packagePath = parent.getElementName().replace('.', '/');
			filePath = '/' + packagePath + '/' + classFile.getElementName();
		} else { // folder
			if (library.isExternal()) {
				filePath = classFile.getPath().toOSString();
			} else {
				IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(classFile.getPath());
				filePath = file.getLocation().toOSString();
			}
		}
		return filePath;
	}
	
	// <<<
}
