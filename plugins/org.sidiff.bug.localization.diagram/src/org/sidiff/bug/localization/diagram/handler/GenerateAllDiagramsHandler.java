package org.sidiff.bug.localization.diagram.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.sirius.common.tools.api.resource.ImageFileFormat;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.sidiff.bug.localization.diagram.neo4j.Neo4jJsonToEcoreUML;
import org.sidiff.bug.localization.diagram.sirius.ModelDiagramCreator;

public class GenerateAllDiagramsHandler extends AbstractHandler {

	private Predicate<String> FILE_PATTERN = (filename) -> filename.endsWith("_diagram.json");

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Object selection = HandlerUtil.getCurrentStructuredSelection(event).getFirstElement();

		if (selection instanceof IContainer) {
			try {
				Job job = Job.create("Generate Diagrams", (ICoreRunnable) monitor -> {
					
					for (IResource resources : ((IContainer) selection).members()) {
						if (FILE_PATTERN.test(resources.getName())) {
							generateDiagram(resources);
						}
					}
				});
				job.schedule();
				
				((IContainer) selection).getProject().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
			} catch (CoreException e1) {
				e1.printStackTrace();
			}
		}

		return null;
	}

	protected void generateDiagram(IResource resources) {
		Job job = Job.create("Generate Diagram: " + resources.getName(), (ICoreRunnable) monitor -> {
			try {
				// Generate .uml file:
				Neo4jJsonToEcoreUML neo4jJsonToEcoreUML = new Neo4jJsonToEcoreUML();
				Resource sliceResource = neo4jJsonToEcoreUML.convert(resources.getLocation().toFile().toPath());
				Model sliceRoot = (Model) sliceResource.getContents().get(0);

				// WORKAROUND: Remove nested packages:
				List<PackageableElement> nestedElements = new ArrayList<>();
				Set<Package> toBeRemoved = new HashSet<>();

				for (EObject modelElement : (Iterable<EObject>) () -> sliceRoot.eAllContents()) {
					if (modelElement instanceof Package) {
						toBeRemoved.add((Package) modelElement);
					} else if (modelElement instanceof PackageableElement) {
						if (toBeRemoved.contains(modelElement.eContainer())) {
							nestedElements.add((PackageableElement) modelElement);
						}
					}
				}

				sliceRoot.getPackagedElements().addAll(nestedElements);

				for (Package packageToBeRemoved : toBeRemoved) {
					EcoreUtil.remove(packageToBeRemoved);
				}

				// Save UML resource:
				sliceResource.setURI(URI
						.createPlatformResourceURI(resources.getFullPath().toPortableString(), true)
						.trimFileExtension().appendFileExtension("uml"));
				sliceResource.save(Collections.EMPTY_MAP);

				// Generate Sirius UML diagram:
				PlatformUI.getWorkbench().getDisplay().asyncExec(() -> {
					try {
						ModelDiagramCreator diagramCreator = new ModelDiagramCreator(sliceRoot, "Class Diagram", monitor);
						diagramCreator.createLayoutedRepresentation(new int[] { 20, 10 }, true, monitor);
						diagramCreator.export(null, ImageFileFormat.SVG, true, true);
						diagramCreator.saveAndCloseEditor();
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		job.schedule();
	}
}
