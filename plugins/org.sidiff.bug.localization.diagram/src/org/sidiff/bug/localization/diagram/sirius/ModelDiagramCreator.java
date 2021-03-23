package org.sidiff.bug.localization.diagram.sirius;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sirius.business.api.dialect.DialectManager;
import org.eclipse.sirius.business.api.helper.SiriusResourceHelper;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.SessionManager;
import org.eclipse.sirius.common.tools.api.resource.ImageFileFormat;
import org.eclipse.sirius.diagram.elk.ElkDiagramLayoutConnector;
import org.eclipse.sirius.tools.api.command.semantic.AddSemanticResourceCommand;
import org.eclipse.sirius.ui.business.api.dialect.DialectUIManager;
import org.eclipse.sirius.ui.business.api.viewpoint.ViewpointSelection;
import org.eclipse.sirius.ui.business.api.viewpoint.ViewpointSelectionCallbackWithConfimation;
import org.eclipse.sirius.ui.business.internal.commands.ChangeViewpointSelectionCommand;
import org.eclipse.sirius.viewpoint.DRepresentation;
import org.eclipse.sirius.viewpoint.description.RepresentationDescription;
import org.eclipse.sirius.viewpoint.description.Viewpoint;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.obeonetwork.dsl.uml2.core.internal.services.UIServices;

/**
 * Creates Sirius diagrams for a given model element and its contained elements.
 * 
 * @author Manuel Ohrndorf
 */
@SuppressWarnings("restriction")
public class ModelDiagramCreator {

	private EObject modelElement;

	private EObject modelElementInSession; // session loads its own resource

	private URI sessionResourceURI;

	private Session session;

	private RepresentationDescription representationDescription;
	
	private DRepresentation representation;
	
	private IEditorPart editor;
	
	private String diagramType;
	
	public ModelDiagramCreator(EObject modelElement, String diagramType, IProgressMonitor monitor) throws IOException {
		this.modelElement = modelElement;
		this.diagramType = diagramType;

		URI modelElementURI = EcoreUtil.getURI((EObject) modelElement);
		this.sessionResourceURI = modelElementURI.trimFragment().trimFileExtension().appendFileExtension("aird");

		createViewpoint(modelElementURI, sessionResourceURI, monitor);

		// Find representation:
		this.session = findSession(modelElementURI, monitor);
		this.modelElementInSession = findModelElementInSession(session, modelElementURI);

		if (modelElementInSession == null) {
			throw new IOException("Something went wrong. Try to delete .aird diagram and retry!");
		}

		this.representationDescription = findRepresentation(session, modelElementInSession, diagramType);
	}
	
	public IEditorPart createLayoutedRepresentation(int[] fixedNodeSize, boolean openEditor, IProgressMonitor monitor) throws IOException {
		int[] defaultNodeSize = null;
		
		try {
			// TODO: Workaround force fixed node sizes
			if (fixedNodeSize != null) {
				ElkDiagramLayoutConnector.NO_NODE_LAYOUT = true;
				defaultNodeSize = new int[] {UIServices.DEFAULT_WIDTH , UIServices.DEFAULT_HEIGHT};
				UIServices.DEFAULT_WIDTH = fixedNodeSize[0];
				UIServices.DEFAULT_HEIGHT = fixedNodeSize[1];
			}
			
			// Create diagrams:
			// TODO: Layout without opening the editor?!
			String name = createRepresentation(monitor);

			// open editor:
			if (openEditor) {
				this.editor = openRepresentation(getRepresentationDescription(), name, monitor);
			}
		} finally {
			 // TODO: Workaround force fixed node sizes
			if (fixedNodeSize != null) {
				ElkDiagramLayoutConnector.NO_NODE_LAYOUT = true;
				UIServices.DEFAULT_WIDTH = defaultNodeSize[0];
				UIServices.DEFAULT_HEIGHT = defaultNodeSize[1];
			}
		}
		return null;
	}
	
	public void saveAndCloseEditor() {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		page.saveEditor(editor, false);
		
		for (IEditorReference editorRef : PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences()) {
			if (editorRef.getEditor(false).equals(editor)) {
				page.closeEditors(new IEditorReference[] {editorRef}, false);
			}
		}
	}

	public String createRepresentation(IProgressMonitor monitor) {
		String name = getUniqueDiagramName(modelElementInSession, session, representationDescription);
		
		SiriusUtil.edit(session.getTransactionalEditingDomain(), () -> {
			this.representation = DialectManager.INSTANCE.createRepresentation(name, modelElementInSession, representationDescription, session, monitor);
		});
		
		return name;
	}
	
	public IEditorPart openRepresentation(RepresentationDescription description, String name, IProgressMonitor monitor) {
		for (DRepresentation diagramRepresentation : DialectManager.INSTANCE.getRepresentations(description, session)) {
			if (diagramRepresentation.getName().equals(name)) {
				DialectUIManager dialectUIManager = DialectUIManager.INSTANCE;

				IEditorPart editor = dialectUIManager.openEditor(session, diagramRepresentation, monitor);
				return editor;
			}
		}
		return null;
	}

	private void createViewpoint(URI modelElementURI, URI sessionResourceURI, IProgressMonitor monitor) {

		// create viewpoint:
		Session session = SessionManager.INSTANCE.getSession(sessionResourceURI, monitor);

		// adding the resource also to Sirius session:
		AddSemanticResourceCommand addCommandToSession = new AddSemanticResourceCommand(session,
				modelElementURI.trimFragment(), monitor);
		session.getTransactionalEditingDomain().getCommandStack().execute(addCommandToSession);

		// find and add viewpoint:
		Set<Viewpoint> availableViewpoints = ViewpointSelection.getViewpoints(modelElementURI.fileExtension());
		Set<Viewpoint> viewpoints = new HashSet<Viewpoint>();

		for (Viewpoint viewpoint : availableViewpoints) {
			viewpoints.add(SiriusResourceHelper.getCorrespondingViewpoint(session, viewpoint));
		}

		RecordingCommand command = new ChangeViewpointSelectionCommand(session,
				new ViewpointSelectionCallbackWithConfimation(), viewpoints, new HashSet<Viewpoint>(), true, monitor);
		TransactionalEditingDomain domain = session.getTransactionalEditingDomain();
		domain.getCommandStack().execute(command);

		// open the session and add it to the session manager:
		session.open(monitor);
	}

	private Session findSession(URI modelElementURI, IProgressMonitor monitor) {
		return SessionManager.INSTANCE
				.getSession(modelElementURI.trimFragment().trimFileExtension().appendFileExtension("aird"), monitor);
	}

	private EObject findModelElementInSession(Session session, URI modelElementURI) {
		return session.getSemanticResources().iterator().next().getEObject(modelElementURI.fragment());
	}

	private RepresentationDescription findRepresentation(Session session, EObject element, String diagramType) {
		Collection<RepresentationDescription> descriptions = DialectManager.INSTANCE
				.getAvailableRepresentationDescriptions(session.getSelectedViewpoints(false), element);

		for (RepresentationDescription representationDescription : descriptions) {
			if (representationDescription.getName().equals(diagramType)) {
				return representationDescription;
			}
		}

		return null;
	}

	protected String getUniqueDiagramName(EObject modelElement, Session session,
			RepresentationDescription description) {
		String name = getDiagramName(modelElement);
		int count = 0;

		for (DRepresentation diagramRepresentation : DialectManager.INSTANCE.getRepresentations(description, session)) {
			if (diagramRepresentation.getName().equals(name)
					|| ((count > 0) && diagramRepresentation.getName().equals(name + " (" + count + ")"))) {
				++count;
			}
		}

		if (count > 0) {
			name = name + " (" + count + ")";
		}

		return name;
	}

	protected String getDiagramName(EObject modelElement) {
		return modelElement.eResource().getURI().trimFileExtension().lastSegment();
	}
	
	public void export(IPath outputPath, ImageFileFormat imageFormat, boolean exportToHtml, boolean exportDecorations) {
		if (outputPath == null) {
			outputPath = ResourcesPlugin.getWorkspace().getRoot()
					.findMember(sessionResourceURI.toPlatformString(true))
					.getParent().getLocation();
		}
		new DiagramExportAction(session, Collections.singleton(representation), 
				outputPath, imageFormat, exportToHtml, exportDecorations);
	}

	public EObject getModelElement() {
		return modelElement;
	}

	public EObject getModelElementInSession() {
		return modelElementInSession;
	}

	public URI getSessionFile() {
		return sessionResourceURI;
	}

	public RepresentationDescription getRepresentationDescription() {
		return representationDescription;
	}

	public DRepresentation getRepresentation() {
		return representation;
	}

	public String getDiagramType() {
		return diagramType;
	}
}
