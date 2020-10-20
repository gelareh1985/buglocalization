package org.sidiff.bug.localization.dataset.graph;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.sidiff.bug.localization.common.utilities.emf.ModelUtil;
import org.sidiff.bug.localization.common.utilities.java.JUtil;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.reports.model.BugReport;
import org.sidiff.bug.localization.dataset.reports.model.BugReportComment;
import org.sidiff.bug.localization.dataset.systemmodel.Change;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.View;
import org.sidiff.bug.localization.dataset.workspace.model.Project;
import org.sidiff.bug.localization.model2adjlist.converter.Model2AdjListConverter;
import org.sidiff.bug.localization.model2adjlist.converter.ModelElement2NumberConverter;
import org.sidiff.bug.localization.model2adjlist.converter.impl.Model2AdjListConverterImpl;
import org.sidiff.bug.localization.model2adjlist.converter.impl.ModelElement2NumberConverterImpl;
import org.sidiff.bug.localization.model2adjlist.format.ModelAdjacencyList;
import org.sidiff.bug.localization.model2adjlist.util.Model2AdjacencyListUtil;

public class BugLocalizationGraphConstructor {

	private Version buggyVersion;
	
	private Version fixedVersion;
	
	private Project project;
	
	private SystemModel buggySystemModel;
	
	private Set<EObject> bugLocations;
	
	private BugReportNode bugReportNode;
	
	private Set<EObject> bugLocalizationGraph;
	
	public BugLocalizationGraphConstructor(Version buggyVersion, Version fixedVersion, Project project, SystemModel buggySystemModel) {
		this.buggyVersion = buggyVersion;
		this.fixedVersion = fixedVersion;
		this.project = project;
		this.buggySystemModel = buggySystemModel;
		this.bugLocations = getBugLocations(buggySystemModel);
	}
	
	public void construct() {
		// NOTE: The bug report is stored for the fixed version.
		this.bugLocalizationGraph = createBugLocalizationGraph(buggySystemModel);
		this.bugReportNode = createBugReportNode(fixedVersion.getBugReport(), bugLocations);
	}
	
	protected BugReportNode createBugReportNode(BugReport bugReport, Set<EObject> locations) {
		BugReportNode bugReportNode = BugLocalizationGraphFactory.eINSTANCE.createBugReportNode();
		bugReportNode.getLocations().addAll(locations);
		bugReportNode.setId(bugReport.getId());
		bugReportNode.setSummary(bugReport.getSummary());
		bugReportNode.getComments().addAll(bugReport.getComments().stream().map(BugReportComment::getText).collect(Collectors.toList()));
		
		return bugReportNode;
	}
	
	protected Set<EObject> getBugLocations(SystemModel systemModel) {
		Set<EObject> bugLocations = new LinkedHashSet<>();
		
		for (View view : systemModel.getViews())  {
			view.getChanges().stream().map(Change::getLocation).forEach(bugLocations::add);
		}
		
		return bugLocations;
	}
	
	protected Set<EObject> createBugLocalizationGraph(SystemModel systemModel) {
		Set<EObject> bugLocalizationGraph = new LinkedHashSet<>();
		
		for (View view : systemModel.getViews())  {
			Set<EObject> bugLocations = view.getChanges().stream().map(Change::getLocation).collect(Collectors.toSet());
			bugLocalizationGraph.addAll(createBugLocalizationGraph(view.getModel(), bugLocations));
		}
		
		return bugLocalizationGraph;
	}

	protected Set<EObject> createBugLocalizationGraph(EObject model, Set<EObject> locations) {
		Set<EObject> modelElements = new LinkedHashSet<>();
		Set<EObject> childTree = new LinkedHashSet<>();
		
		// Locations:
		modelElements.addAll(locations);

		/*
		 *  Parents/Container Elements:
		 */
		int parentLevels = 4;
		
		for (EObject location : locations) {
			ModelUtil.collectParents(modelElements, location, parentLevels);
		}
		
		/*
		 * Child Elements:
		 */
		int childLevels = 3;
		
		for (EObject location : locations) {
			ModelUtil.collectOutgoingReferences(childTree, location, childLevels, e -> e.isContainment());
		}
		
		modelElements.addAll(childTree);
		
		/*
		 * Outgoing Elements of Child Tree:
		 */
		int outgoingDistance = 2;
		
		for (EObject modelElement : childTree) {
			ModelUtil.collectOutgoingReferences(modelElements, modelElement, outgoingDistance, e -> !e.isContainment());
		}
		
		/*
		 * Incoming Elements of Child Tree (within Model):
		 */
		int incomingDistance = 1;
		
		ModelUtil.collectIncomingReferences(modelElements, model, childTree, incomingDistance, e -> !e.isContainment());
		
		return modelElements;
	}

	/**
	 * @param folder    The path to the storage folder.
	 * @param fileNames The name of the file(s) without file extension.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void save(Path folder, String fileNames) throws FileNotFoundException, IOException {
		
		if ((bugReportNode == null) || (bugLocalizationGraph == null)) {
			construct();
		}
		
		// Convert to adjacency list:
		ModelElement2NumberConverter element2Number = new ModelElement2NumberConverterImpl();
		Model2AdjListConverter model2AdjList = new Model2AdjListConverterImpl(element2Number);

		Iterable<EObject> graphNodes = JUtil.merge(Collections.singletonList(bugReportNode), bugLocalizationGraph);
		ModelAdjacencyList adjacencyList = model2AdjList.convert(graphNodes);

		// Output:
		Path path = folder.resolve(fileNames + ".edgelist");
		Model2AdjacencyListUtil.save(path.toFile(), adjacencyList);
	}

	public Version getBuggyVersion() {
		return buggyVersion;
	}

	public Version getFixedVersion() {
		return fixedVersion;
	}

	public Project getProject() {
		return project;
	}

	public SystemModel getBuggySystemModel() {
		return buggySystemModel;
	}

	public Set<EObject> getBugLocations() {
		return bugLocations;
	}

	public BugReportNode getBugReportNode() {
		return bugReportNode;
	}

	public Set<EObject> getBugLocalizationGraph() {
		return bugLocalizationGraph;
	}
}
