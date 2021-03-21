package org.sidiff.bug.localization.diagram.handler;

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.sidiff.bug.localization.common.utilities.emf.EMFHandlerUtil;
import org.sidiff.bug.localization.diagram.sirius.ModelDiagramCreator;

public class CreateClassDiagramHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Object selected = getSelection(event);
		
		if (selected instanceof IFile) {
			Resource resource = EMFHandlerUtil.getSelection(event);
			
			if (!resource.getContents().isEmpty()) {
				selected = resource.getContents().get(0);
			}
		}
		
		if (selected instanceof EObject) {
			IProgressMonitor monitor = new NullProgressMonitor();
			
			// new diagram:
			try {
				ModelDiagramCreator creator = new ModelDiagramCreator((EObject) selected, "Class Diagram", monitor);
				creator.createLayoutedRepresentation(new int[] {20, 10}, true, monitor);
			} catch (IOException e) {
				showError(e.getMessage());
			}
		}
		
		return null;
	}
	
	protected Object getSelection(ExecutionEvent event) {
		ISelection selection = HandlerUtil.getCurrentSelection(event);

		if (!selection.isEmpty() && (selection instanceof IStructuredSelection)) {
			return ((IStructuredSelection) selection).getFirstElement();
		}
		
		return null;
	}
	
	private static void showError(String message) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog.openError(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart().getTitle(),
						message);
			}
		});
	}
}
