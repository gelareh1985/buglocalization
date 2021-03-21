package org.sidiff.bug.localization.diagram.sirius;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.sirius.business.api.dialect.command.RefreshRepresentationsCommand;
import org.eclipse.sirius.diagram.ui.edit.api.part.AbstractBorderedDiagramElementEditPart;
import org.eclipse.sirius.diagram.ui.internal.edit.parts.DNodeListEditPart;
import org.eclipse.sirius.diagram.ui.tools.api.editor.DDiagramEditor;
import org.eclipse.sirius.viewpoint.DRepresentation;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

/**
 * Helper functions to work with EMF sirius diagrams.
 * 
 * @author Manuel Ohrndorf
 */
@SuppressWarnings({ "restriction", "deprecation" })
public class SiriusUtil {
	
	public static void edit(TransactionalEditingDomain editingDomain, Runnable runnable) {
		if (editingDomain != null) {
			editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {

				@Override
				protected void doExecute() {
					runnable.run();
				}
			});	
		} else {
			runnable.run();
		}
	}

	public static void edit(EObject modelElement, Runnable runnable) {
		TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(modelElement);
		
		if (editingDomain != null) {
			editingDomain.getCommandStack().execute(new RecordingCommand(editingDomain) {

				@Override
				protected void doExecute() {
					runnable.run();
				}
			});	
		} else {
			runnable.run();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends EObject> T getRoot(IWorkbenchPart part, Class<T> rootType) {

		if (part instanceof DDiagramEditor) {
			DDiagramEditor editor = (DDiagramEditor) part;
			Iterator<Resource> it = editor.getSession().getSemanticResources().iterator();

			if (it.hasNext()) {
				Resource resource = it.next();

				if (!resource.getContents().isEmpty()) {
					EObject content = resource.getContents().get(0);

					if (rootType.isInstance(content)) {
						return (T) content;
					}
				}
			}
		}

		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static List<Object> getSemanticElements(ISelection selection) {
		
		if (selection instanceof StructuredSelection) {
			List<Object> newSelections = new ArrayList<>();

			// Convert GEF-Selections:
			for (Iterator<Object> iterator = ((StructuredSelection) selection).iterator(); iterator.hasNext();) {
				Object remoteSelectedElement = (Object) iterator.next();

				if (remoteSelectedElement instanceof AbstractBorderedDiagramElementEditPart) {
					newSelections.add(getSemanticElement((DNodeListEditPart) remoteSelectedElement));
				} else { 	
					newSelections.add(remoteSelectedElement);
				}
			}
			
			return newSelections;
		}
		
		return Collections.emptyList();
	}

	public static EObject getSemanticElement(AbstractBorderedDiagramElementEditPart element) {
		return ((AbstractBorderedDiagramElementEditPart) element).resolveDiagramElement().getTarget();
	}

	public static void refreshActiveEditor() {
		IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

		if (editor instanceof DDiagramEditor) {
			DDiagramEditor diagramEditor = (DDiagramEditor) editor;

			DRepresentation representation = diagramEditor.getRepresentation();
			// TransactionalEditingDomain domain = TransactionUtil.getEditingDomain(IDDiagramEditPart diagram);
			TransactionalEditingDomain domain = (TransactionalEditingDomain) diagramEditor.getEditingDomain();

			domain.getCommandStack().execute(new RefreshRepresentationsCommand(domain,
					new SubProgressMonitor(new NullProgressMonitor(), 1), representation));
		}
	}
}
