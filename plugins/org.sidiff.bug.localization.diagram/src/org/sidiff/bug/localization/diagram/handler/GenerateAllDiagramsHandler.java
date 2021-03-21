package org.sidiff.bug.localization.diagram.handler;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.sidiff.bug.localization.diagram.neo4j.Neo4jJsonToEcoreUML;
import org.sidiff.bug.localization.diagram.sirius.ModelDiagramCreator;

public class GenerateAllDiagramsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Neo4jJsonToEcoreUML neo4jJsonToEcoreUML = new Neo4jJsonToEcoreUML();
		
		// TODO //
		
		Path diagramJsonPath = Path.of(
				"C:\\Users\\manue\\git\\buglocalization\\plugins\\org.sidiff.bug.localization.prediction\\evaluation\\trained_model_2021-03-13_16-16-02_lr-4_layer300_test\\8493_bug568333_27041a77e8bdc112b51e349f0b43eaac87598d1b_prediction.csv_diagram.json");

		try {
			Resource sliceResource = neo4jJsonToEcoreUML.convert(diagramJsonPath);
			sliceResource.save(Collections.EMPTY_MAP);
			
//			IProgressMonitor monitor = new NullProgressMonitor();
//			
//			EObject sliceRoot = sliceResource.getContents().get(0);
//			ModelDiagramCreator diagramCreator = new ModelDiagramCreator(sliceRoot, "Class Diagram", monitor);
//			diagramCreator.createLayoutedRepresentation(new int[] {20, 10}, true, monitor);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

}
