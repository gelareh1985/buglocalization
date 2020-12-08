package org.sidiff.reverseengineering.java.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.BindingKey;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;

/**
 *  Helper to handle unresolved, recovered binding.
 * 
 * @author Manuel Ohrndorf
 */
public class BindingRecovery {
	
	/**
	 * The corresponding compilation unit.
	 */
	private CompilationUnit compilationUnit;
	
	/**
	 * Imports for binding recovery: Type Name -> Full Qualified Name
	 */
	private Map<String, String> compilationUnitImports;

	public BindingRecovery(CompilationUnit compilationUnit) {
		this.compilationUnit = compilationUnit;
	}

	public String getRecoveredBinding(IBinding binding) {
		if (binding.isRecovered()) {
			ITypeBinding typeBinding = getRecoveredTypeBinding(binding);
			
			String typeName = typeBinding.getName();
			String qualifiedName = getCompilationUnitImports().get(typeName);
			
			if (qualifiedName != null) {
				String recoverdTypeBinding = BindingKey.createTypeBindingKey(qualifiedName);
				
				String originalBindingKey = binding.getKey();
				String substitutedTypeBinding = originalBindingKey.substring(typeBinding.getKey().length(), originalBindingKey.length());
				String recoverdBinding = recoverdTypeBinding + substitutedTypeBinding;
				return recoverdBinding;
			}
		}
		return null;
	}

	private ITypeBinding getRecoveredTypeBinding(IBinding binding) {
		if (binding instanceof ITypeBinding) {
			return (ITypeBinding) binding;
		} else {
			return ((ITypeBinding) binding).getDeclaringClass();
		}
	}
	
	public String[] getRecoveredPackage(IBinding binding) {
		if (binding.isRecovered()) {
			
			ITypeBinding typeBinding = getRecoveredTypeBinding(binding);
			ITypeBinding outerTypeBinding = JavaASTUtil.getOuterTypeBinding(typeBinding);
			
			String qualifiedName = getCompilationUnitImports().get(typeBinding.getName());
			
			if (qualifiedName != null) {
				String outerTypeName = outerTypeBinding.getName();
				String qualifiedPackage = qualifiedName.substring(0, qualifiedName.lastIndexOf(outerTypeName) - 1);
				
				String[] packages = qualifiedPackage.split("\\.");
				return packages;
			}
		}
		return null;
	}
	
	public Map<String, String> getCompilationUnitImports() {
		
		if (compilationUnitImports == null) {
			this.compilationUnitImports = new HashMap<>();
			
			for (Object importDeclaration : compilationUnit.imports()) {
				if (importDeclaration instanceof ImportDeclaration) {
					String qualifiedName = ((ImportDeclaration) importDeclaration).getName().getFullyQualifiedName();
					String typeName = qualifiedName.substring(qualifiedName.lastIndexOf(".") + 1, qualifiedName.length());
					this.compilationUnitImports.put(typeName, qualifiedName);
				}
			}
		}
		
		return compilationUnitImports;
	}
	
	public CompilationUnit getCompilationUnit() {
		return compilationUnit;
	}
}
