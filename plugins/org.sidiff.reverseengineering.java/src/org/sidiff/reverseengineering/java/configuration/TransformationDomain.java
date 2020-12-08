package org.sidiff.reverseengineering.java.configuration;

import org.sidiff.reverseengineering.java.transformation.JavaASTBindingResolver;
import org.sidiff.reverseengineering.java.transformation.JavaASTLibraryModel;
import org.sidiff.reverseengineering.java.transformation.JavaASTProjectModel;
import org.sidiff.reverseengineering.java.transformation.JavaASTTransformation;
import org.sidiff.reverseengineering.java.transformation.JavaASTWorkspaceModel;

public interface TransformationDomain {

	Class<? extends JavaASTTransformation> getDomainSpecificTransformation();
	
	Class<? extends JavaASTBindingResolver> getDomainSpecificBindingResolver();

	Class<? extends JavaASTWorkspaceModel> getDomainSpecificWorkspaceModel();

	Class<? extends JavaASTLibraryModel> getDomainSpecificLibraryModel();

	Class<? extends JavaASTProjectModel> getDomainSpecificProjectModel();
	
}
