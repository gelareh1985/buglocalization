package org.sidiff.bug.localization.dataset.graph;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.sidiff.bug.localization.common.utilities.emf.ModelUtil;
import org.sidiff.bug.localization.common.utilities.java.JUtil;
import org.sidiff.bug.localization.dataset.reports.model.BugReport;
import org.sidiff.bug.localization.dataset.reports.model.BugReportComment;
import org.sidiff.bug.localization.dataset.systemmodel.Change;
import org.sidiff.bug.localization.dataset.systemmodel.SystemModel;
import org.sidiff.bug.localization.dataset.systemmodel.View;

public class BugLocalizationGraphConstructor {
	
	private SystemModel buggySystemModel;
	
	private Set<EObject> bugLocations;
	
	private BugReportNode bugReportNode;
	
	private LocalBugLocalizationGraphSettings loaclGraphSettings;
	
	public static class LocalBugLocalizationGraphSettings {
		public int parentLevels = 4;
		public int childLevels = 3;
		public int outgoingDistance = 2;
		public int incomingDistance = 1;
	}
	
	public BugLocalizationGraphConstructor(BugReport bugReport, SystemModel buggySystemModel) {
		this.buggySystemModel = buggySystemModel;
		this.bugLocations = collectBugLocations(buggySystemModel);
		this.bugReportNode = createBugReportNode(bugReport, bugLocations);
		this.loaclGraphSettings = new LocalBugLocalizationGraphSettings();
	}
	
	protected Set<EObject> collectBugLocations(SystemModel systemModel) {
		Set<EObject> bugLocations = new LinkedHashSet<>();
		
		for (View view : systemModel.getViews())  {
			view.getChanges().stream().map(Change::getLocation).forEach(bugLocations::add);
		}
		
		return bugLocations;
	}

	protected BugReportNode createBugReportNode(BugReport bugReport, Set<EObject> locations) {
		BugReportNode bugReportNode = BugLocalizationGraphFactory.eINSTANCE.createBugReportNode();
		bugReportNode.getLocations().addAll(locations);
		bugReportNode.setId(bugReport.getId());
		bugReportNode.setSummary(bugReport.getSummary());
		bugReportNode.getComments().addAll(bugReport.getComments().stream().map(BugReportComment::getText).collect(Collectors.toList()));
		
		return bugReportNode;
	}
	
	public Iterable<EObject> createBugLocalizationGraph() {
		Iterable<EObject> bugLocalizationGraph = Collections.singletonList(bugReportNode);
		
		for (View view : buggySystemModel.getViews())  {
			bugLocalizationGraph = JUtil.concatIerables(bugLocalizationGraph, () -> view.getModel().eAllContents());
		}
		
		return bugLocalizationGraph;
	}

	public Set<EObject> createLocalBugLocalizationGraph() {
		Set<EObject> bugLocalizationGraph = new LinkedHashSet<>(); // keeps order
		bugLocalizationGraph.add(bugReportNode); // bug report as first node
		createLocalBugLocalizationGraph(bugLocalizationGraph);
		return bugLocalizationGraph;
	}
	
	protected void createLocalBugLocalizationGraph(Set<EObject> bugLocalizationGraph) {
		for (View view : buggySystemModel.getViews())  {
			Set<EObject> bugLocations = view.getChanges().stream().map(Change::getLocation).collect(Collectors.toSet());
			bugLocalizationGraph.addAll(createLocalBugLocalizationGraph(view.getModel(), bugLocations));
		}
	}

	protected Set<EObject> createLocalBugLocalizationGraph(EObject model, Set<EObject> locations) {
		Set<EObject> modelElements = new LinkedHashSet<>();
		Set<EObject> childTree = new LinkedHashSet<>();
		
		// Locations:
		modelElements.addAll(locations);

		/*
		 *  Parents/Container Elements:
		 */
		
		for (EObject location : locations) {
			ModelUtil.collectParents(modelElements, location, loaclGraphSettings.parentLevels);
		}
		
		/*
		 * Child Elements:
		 */
		
		for (EObject location : locations) {
			ModelUtil.collectOutgoingReferences(childTree, location, loaclGraphSettings.childLevels, e -> e.isContainment());
		}
		
		modelElements.addAll(childTree);
		
		/*
		 * Outgoing Elements of Child Tree:
		 */
		
		for (EObject modelElement : childTree) {
			ModelUtil.collectOutgoingReferences(modelElements, modelElement, loaclGraphSettings.outgoingDistance, e -> !e.isContainment());
		}
		
		/*
		 * Incoming Elements of Child Tree (within Model):
		 */
		
		ModelUtil.collectIncomingReferences(modelElements, model, childTree, loaclGraphSettings.incomingDistance, e -> !e.isContainment());
		
		return modelElements;
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
	
	public LocalBugLocalizationGraphSettings getLoaclGraphSettings() {
		return loaclGraphSettings;
	}

	public void setLoaclGraphSettings(LocalBugLocalizationGraphSettings loaclGraphSettings) {
		this.loaclGraphSettings = loaclGraphSettings;
	}
}
