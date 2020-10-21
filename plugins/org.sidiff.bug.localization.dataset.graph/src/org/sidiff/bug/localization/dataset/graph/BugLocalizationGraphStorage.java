package org.sidiff.bug.localization.dataset.graph;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.sidiff.bug.localization.dataset.graph.data.model.Edges;
import org.sidiff.bug.localization.dataset.graph.data.model.Nodes;
import org.sidiff.bug.localization.dataset.graph.data.model.converters.basics.ModelElement2Comment;
import org.sidiff.bug.localization.dataset.graph.data.model.converters.basics.ModelElement2ID;
import org.sidiff.bug.localization.dataset.graph.data.model.converters.basics.ModelReference2SourceID;
import org.sidiff.bug.localization.dataset.graph.data.model.converters.basics.ModelReference2TargetID;
import org.sidiff.bug.localization.dataset.graph.data.model.converters.basics.ModelReferenceTestSubGraph;
import org.sidiff.bug.localization.dataset.graph.data.model.converters.signature.ModelElement2Signature;
import org.sidiff.bug.localization.dataset.graph.data.model.converters.signature.ModelElement2SignatureBugReport;
import org.sidiff.bug.localization.dataset.graph.data.model.converters.signature.ModelElement2SignatureName;

public class BugLocalizationGraphStorage {

	private String bugTag = "LOCATION";
	
	private Set<EObject> bugLocations;
	
	private Iterable<EObject> bugLocalizationGraph;
	
	public BugLocalizationGraphStorage(Set<EObject> bugLocations, Iterable<EObject> bugLocalizationGraph) {
		this.bugLocations = bugLocations;
		this.bugLocalizationGraph = bugLocalizationGraph;
	}

	public String getBugTag() {
		return bugTag;
	}

	public void setBugTag(String bugTag) {
		this.bugTag = bugTag;
	}
	
	/**
	 * @param folder    The path to the storage folder.
	 * @param fileNames The name of the file(s) without file extension.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void save(Path folder, String fileNames) throws FileNotFoundException, IOException {
		
		/*
		 *  Convert Nodes:
		 */
		ModelElement2ID modelElement2ID = new ModelElement2ID();
		
		ModelElement2Signature modelElement2Signature = new ModelElement2Signature(new ModelElement2SignatureName());
		modelElement2Signature.addTypedConverter(BugLocalizationGraphPackage.eINSTANCE.getBugReportNode(), new ModelElement2SignatureBugReport());
		
		// Tag bug locations as comments:
		ModelElement2Comment modelElement2Comment = new ModelElement2Comment();
		
		for (EObject bugLocation : bugLocations) {
			modelElement2Comment.addComment(bugLocation, bugTag);
		}
		
		// NOTE: The bug localization graph also contains the bug report node.
		Nodes nodesData = new Nodes(modelElement2Comment, modelElement2ID, modelElement2Signature);
		nodesData.addNodes(bugLocalizationGraph);
		
		Path nodesPath = folder.resolve(fileNames + ".nodelist");
		nodesData.save(nodesPath);
		
		/*
		 *  Convert Edges:
		 */
		ModelReference2SourceID modelReference2SourceID = new ModelReference2SourceID(modelElement2ID);
		ModelReference2TargetID modelReference2TargetID = new ModelReference2TargetID(modelElement2ID);
		
		ModelReferenceTestSubGraph modelReferenceTestSubGraph = new ModelReferenceTestSubGraph(modelElement2ID);
		
		// NOTE: The bug localization graph also contains the bug report node.
		Edges edgesDate = new Edges(modelReferenceTestSubGraph, modelReference2SourceID, modelReference2TargetID);
		edgesDate.addEdges(bugLocalizationGraph, false);
		
		Path edgesPath = folder.resolve(fileNames + ".edgelist");
		edgesDate.save(edgesPath);
	}
}
