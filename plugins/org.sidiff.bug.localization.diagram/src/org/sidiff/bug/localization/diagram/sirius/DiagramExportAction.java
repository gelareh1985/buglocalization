package org.sidiff.bug.localization.diagram.sirius;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.common.tools.api.resource.ImageFileFormat;
import org.eclipse.sirius.ui.tools.api.actions.export.ExportAction;
import org.eclipse.sirius.viewpoint.DRepresentation;

public class DiagramExportAction extends ExportAction {

	public DiagramExportAction(Session session, Collection<DRepresentation> dRepresentationsToExportAsImage,
			IPath outputPath, ImageFileFormat imageFormat, boolean exportToHtml, boolean exportDecorations) {
		
		super(session, dRepresentationsToExportAsImage, outputPath, imageFormat, exportToHtml, exportDecorations);
		
		try {
			execute(new NullProgressMonitor());
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}

}
