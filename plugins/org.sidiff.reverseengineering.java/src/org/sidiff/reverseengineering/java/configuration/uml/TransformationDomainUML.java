package org.sidiff.reverseengineering.java.configuration.uml;

import org.sidiff.reverseengineering.java.configuration.TransformationDomain;
import org.sidiff.reverseengineering.java.transformation.JavaASTBindingResolver;
import org.sidiff.reverseengineering.java.transformation.JavaASTLibraryModel;
import org.sidiff.reverseengineering.java.transformation.JavaASTProjectModel;
import org.sidiff.reverseengineering.java.transformation.JavaASTTransformation;
import org.sidiff.reverseengineering.java.transformation.JavaASTWorkspaceModel;
import org.sidiff.reverseengineering.java.transformation.uml.JavaASTBindingResolverUML;
import org.sidiff.reverseengineering.java.transformation.uml.JavaASTLibraryModelUML;
import org.sidiff.reverseengineering.java.transformation.uml.JavaASTProjectModelUML;
import org.sidiff.reverseengineering.java.transformation.uml.JavaASTTransformationUML;
import org.sidiff.reverseengineering.java.transformation.uml.JavaASTWorkspaceModelUML;

public class TransformationDomainUML implements TransformationDomain {

	@Override
	public Class<? extends JavaASTTransformation> getDomainSpecificTransformation() {
		return JavaASTTransformationUML.class;
	}

	@Override
	public Class<? extends JavaASTBindingResolver> getDomainSpecificBindingResolver() {
		return JavaASTBindingResolverUML.class;
	}

	@Override
	public Class<? extends JavaASTWorkspaceModel> getDomainSpecificWorkspaceModel() {
		return JavaASTWorkspaceModelUML.class;
	}

	@Override
	public Class<? extends JavaASTLibraryModel> getDomainSpecificLibraryModel() {
		return JavaASTLibraryModelUML.class;
	}

	@Override
	public Class<? extends JavaASTProjectModel> getDomainSpecificProjectModel() {
		return JavaASTProjectModelUML.class;
	}
	
	public static String getModelFileExtension() {
		return "uml";
	}
	
}
