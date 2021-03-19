package org.sidiff.reverseengineering.java.transformation;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.common.util.URI;
import org.eclipse.jdt.core.dom.IBinding;
import org.sidiff.reverseengineering.java.configuration.TransformationSettings;

import com.google.inject.Inject;

/**
 * Maps Java AST bindings to EMF XMI object ID bindings.
 * 
 * @author Manuel Ohrndorf
 */
public class JavaASTBindingTranslator {
	
	private Pattern genericParameter = Pattern.compile("(.*?)(<.*?>)(.*?)");
	
	private TransformationSettings settings;
	
	/**
	 * @param settings The transformation settings.
	 */
	@Inject
	public JavaASTBindingTranslator(TransformationSettings settings) {
		this.settings = settings;
	}

	/**
	 * @param projectName The name of the containing project
	 * @param binding The Java AST binding.
	 * @return The corresponding unique binding key.
	 */
	public String getBindingKey(String projectName, IBinding binding) {
		return getBindingKey(projectName, binding.getKey());
	}
	
	/**
	 * @param projectName The name of the containing project
	 * @param binding The Java AST binding.
	 * @return The corresponding unique binding key.
	 */
	public String getBindingKey(String projectName, String bindingKey) {
		
		// Include project in namespace.
		String uniqueBindingKey = projectName + "/" + bindingKey;
		
		// Characters <> from Java generic are not allowed as XML attributes.
		// Generic are not needed for a unique key -> remove <E>.
		Matcher matcher = genericParameter.matcher(uniqueBindingKey);
		uniqueBindingKey = matcher.replaceAll("$1$3");
		
		return uniqueBindingKey;
	}
	
	/**
	 * @param externalProjectName The name of the external project containing the
	 * @param externalBinding     The Java AST binding.
	 * @param externalPath        The path to the external model.
	 * @param localPath           The local path of the current model.
	 * @return The URI to the external model.
	 */
	public URI getExternalURI(String externalProjectName, IBinding externalBinding, String[] externalPath, String[] localPath) {
		Path relativePath = Paths.get("", localPath).getParent().relativize(Paths.get("", externalPath));
		URI modelURI = URI.createURI("", true);
		
		for (Path path : relativePath) {
			modelURI = modelURI.appendSegment(path.toString());
		}
		
		return getURI(modelURI, externalProjectName, externalBinding);
	}
	
	/**
	 * @param modelURI    The URI to the model resource.
	 * @param projectName The name of the containing project
	 * @param binding     The Java AST binding.
	 * @return The URI with the corresponding fragment.
	 */
	public URI getURI(URI modelURI, String projectName, IBinding binding) {
		return modelURI.appendFragment(getBindingKey(projectName, binding));
	}

	public TransformationSettings getSettings() {
		return settings;
	}
}
