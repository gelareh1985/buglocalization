package org.sidiff.reverseengineering.java.configuration;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.sidiff.reverseengineering.java.transformation.JavaASTBindingResolver;
import org.sidiff.reverseengineering.java.transformation.JavaASTBindingTranslator;
import org.sidiff.reverseengineering.java.transformation.JavaASTLibraryModel;
import org.sidiff.reverseengineering.java.transformation.JavaASTProjectModel;
import org.sidiff.reverseengineering.java.transformation.JavaASTTransformation;
import org.sidiff.reverseengineering.java.transformation.JavaASTWorkspaceModel;
import org.sidiff.reverseengineering.java.util.EMFHelper;
import org.sidiff.reverseengineering.java.util.JavaParser;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class TransformationModule extends AbstractModule {
	
	private TransformationSettings settings;
	
	private TransformationDomain transformationDomain;
	
	public TransformationModule(TransformationSettings settings, TransformationDomain transformationDomain) {
		this.settings = settings;
		this.transformationDomain = transformationDomain;
	}
	
	@Override
	protected void configure() {
		configureSettings();
		configureEMFHelper();
		configureJavaParser();
		configureResourceSet();
		configureBindingTranslator();
		configureJavaASTTransformation(transformationDomain.getDomainSpecificTransformation());
		configureJavaASTBindingResolver(transformationDomain.getDomainSpecificBindingResolver());
		configureJavaASTWorkspaceModel(transformationDomain.getDomainSpecificWorkspaceModel());
		configureJavaASTLibraryModel(transformationDomain.getDomainSpecificLibraryModel());
		configureJavaASTProjectModel(transformationDomain.getDomainSpecificProjectModel());
	}
	
	/*
	 * Instances:
	 */
	
	protected void configureSettings() {
		bind(TransformationSettings.class).toInstance(settings);
	}
	
	/*
	 *  Singletons:
	 */
	
	protected void configureEMFHelper() {
		bind(EMFHelper.class).in(Singleton.class);
	}
	
	protected void configureJavaParser() {
		bind(JavaParser.class).in(Singleton.class);
	}
	
	protected void configureBindingTranslator() {
		 bind(JavaASTBindingTranslator.class).in(Singleton.class);
	}
	
	/*
	 * Type Bindings:
	 */
	
	protected void configureResourceSet() {
		bind(ResourceSet.class).to(ResourceSetImpl.class);
	}
	
	/*
	 * Provider Binding:
	 */
	
	protected void configureJavaASTTransformation(Class<? extends JavaASTTransformation> domainSpecificTransformation) {
		install(new FactoryModuleBuilder()
			     .implement(JavaASTTransformation.class, domainSpecificTransformation)
			     .build(JavaASTTransformationFactory.class));
	}
	
	public interface JavaASTTransformationFactory {
		JavaASTTransformation create(CompilationUnit compilationUnit, JavaASTBindingResolver bindings);
	}

	protected void configureJavaASTBindingResolver(Class<? extends JavaASTBindingResolver> domainSpecificBindingResolver) {
		install(new FactoryModuleBuilder()
			     .implement(JavaASTBindingResolver.class, domainSpecificBindingResolver)
			     .build(JavaASTBindingResolverFactory.class));
	}

	public interface JavaASTBindingResolverFactory {
		JavaASTBindingResolver create(CompilationUnit compilationUnit, Set<String> workspaceProjectScope, JavaASTLibraryModel libraryModel);
	}

	protected void configureJavaASTWorkspaceModel(Class<? extends JavaASTWorkspaceModel> domainSpecificWorkspaceModel) {
		install(new FactoryModuleBuilder()
			     .implement(JavaASTWorkspaceModel.class, domainSpecificWorkspaceModel)
			     .build(JavaASTWorkspaceModelFactory.class));
	}

	public interface JavaASTWorkspaceModelFactory {
		JavaASTWorkspaceModel create(XMLResource workspaceModel, String name);
	}
	
	protected void configureJavaASTLibraryModel(Class<? extends JavaASTLibraryModel> domainSpecificLibraryModel) {
		install(new FactoryModuleBuilder()
			     .implement(JavaASTLibraryModel.class, domainSpecificLibraryModel)
			     .build(JavaASTLibraryModelFactory.class));
	}
	
	public interface JavaASTLibraryModelFactory {
		JavaASTLibraryModel create(XMLResource libraryModel);
	}
	
	protected void configureJavaASTProjectModel(Class<? extends JavaASTProjectModel> domainSpecificProjectModel) {
		install(new FactoryModuleBuilder()
			     .implement(JavaASTProjectModel.class, domainSpecificProjectModel)
			     .build(JavaASTProjectModelFactory.class));
	}
	
	public interface JavaASTProjectModelFactory {
		JavaASTProjectModel create(XMLResource projectModel, IProject project);
	}
}
