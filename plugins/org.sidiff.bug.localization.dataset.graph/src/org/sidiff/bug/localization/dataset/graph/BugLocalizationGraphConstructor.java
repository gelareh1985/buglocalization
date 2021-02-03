package org.sidiff.bug.localization.dataset.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.uml2.uml.UMLPackage;
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
	
	public static class KNeighbourSettings {
		
		public int parentLevels = 4;
		public int childLevels = 3;
		public int outgoingDistance = 2; // on children
		public int incomingDistance = 1; // on children
		
		public KNeighbourSettings() {
		}
		
		public KNeighbourSettings(int parentLevels, int childLevels, int outgoingDistance, int incomingDistance) {
			this.parentLevels = parentLevels;
			this.childLevels = childLevels;
			this.outgoingDistance = outgoingDistance;
			this.incomingDistance = incomingDistance;
		}
	}
	
	/*
	 *  Type specific settings:
	 */
	
	private KNeighbourSettings type_model = new KNeighbourSettings(0, 1, 0, 0);
	
	private KNeighbourSettings type_package = new KNeighbourSettings(5, 1, 0, 0);
	
	private KNeighbourSettings type_class = new KNeighbourSettings(5, 2, 1, 1);
	
	private KNeighbourSettings type_operation = new KNeighbourSettings(5, 3, 2, 1);
	
	private KNeighbourSettings type_property = new KNeighbourSettings(5, 1, 2, 1);
	
	private Map<EClass, KNeighbourSettings> type2kNeighbourSettings = new HashMap<>();
	{
		type2kNeighbourSettings.put(UMLPackage.eINSTANCE.getModel(), type_model);
		type2kNeighbourSettings.put(UMLPackage.eINSTANCE.getPackage(), type_package);
		type2kNeighbourSettings.put(UMLPackage.eINSTANCE.getClass_(), type_class);
		type2kNeighbourSettings.put(UMLPackage.eINSTANCE.getOperation(), type_operation);
		type2kNeighbourSettings.put(UMLPackage.eINSTANCE.getProperty(), type_property);
	}
	
	public BugLocalizationGraphConstructor(BugReport bugReport, SystemModel buggySystemModel) {
		this.buggySystemModel = buggySystemModel;
		this.bugLocations = collectBugLocations(buggySystemModel);
		this.bugReportNode = createBugReportNode(bugReport, bugLocations);
	}
	
	protected Set<EObject> collectBugLocations(SystemModel systemModel) {
		Set<EObject> bugLocations = new LinkedHashSet<>();
		
		for (View view : systemModel.getViews())  {
			view.getChanges().stream().map(Change::getLocation).forEach(bugLocations::add);
		}
		
		if (bugLocations.contains(null)) {
			bugLocations.remove(null);
			System.err.println("Bug Report Contains Null-Location");
		}
		return bugLocations;
	}

	protected Set<EObject> collectBugReportGraph(boolean bugReportComments) {
		Set<EObject> bugGraph = new LinkedHashSet<>(); // keeps order
		bugGraph.add(bugReportNode); // bug report as first node
		
		if (bugReportComments) {
			bugGraph.addAll(bugReportNode.getComments());
		}
		
		return bugGraph;
	}

	protected BugReportNode createBugReportNode(BugReport bugReport, Set<EObject> locations) {
		BugReportNode bugReportNode = BugLocalizationGraphFactory.eINSTANCE.createBugReportNode();
		bugReportNode.getLocations().addAll(locations);
		bugReportNode.setId(bugReport.getId());
		bugReportNode.setSummary(bugReport.getSummary());
		
		for (BugReportComment bugReportComment : bugReport.getComments()) {
			BugReportCommentNode bugReportCommentNode = BugLocalizationGraphFactory.eINSTANCE.createBugReportCommentNode();
			bugReportCommentNode.setComment(bugReportComment.getText());
			bugReportNode.getComments().add(bugReportCommentNode);
		}
		
		return bugReportNode;
	}
	
	public Iterable<EObject> createFullBugLocalizationGraph(boolean bugReportComments) {
		Iterable<EObject> bugLocalizationGraph = collectBugReportGraph(bugReportComments);
		
		for (View view : buggySystemModel.getViews())  {
			bugLocalizationGraph = JUtil.concatIerables(bugLocalizationGraph, () -> view.getModel().eAllContents());
		}
		
		return bugLocalizationGraph;
	}

	public Set<EObject> createPositiveSampleBugLocalizationGraph(boolean bugReportComments) {
		Set<EObject> bugLocalizationGraph = collectBugReportGraph(bugReportComments); // keeps order
		createPositiveSampleBugLocalizationGraph(bugLocalizationGraph);
		return bugLocalizationGraph;
	}
	
	private void createPositiveSampleBugLocalizationGraph(Set<EObject> bugLocalizationGraph) {
		for (View view : buggySystemModel.getViews())  {
			Set<EObject> bugLocations = view.getChanges().stream().map(Change::getLocation).collect(Collectors.toSet());
			bugLocalizationGraph.addAll(createLocalBugLocalizationGraph(view.getModel(), bugLocations));
		}
	}
	
	public Set<EObject> createNegativeSampleBugLocalizationGraph(boolean bugReportComments, Set<EObject> negativeSampleBugLocations) {
		
		Set<EObject> bugLocalizationGraph = collectBugReportGraph(bugReportComments); // keeps order
		createNegativeSampleBugLocalizationGraph(bugLocalizationGraph, negativeSampleBugLocations);
		return bugLocalizationGraph;
	}
	
	private void createNegativeSampleBugLocalizationGraph(Set<EObject> bugLocalizationGraph, Set<EObject> negativeSampleBugLocations) {
		
		for (View view : buggySystemModel.getViews())  {
			bugLocalizationGraph.addAll(createLocalBugLocalizationGraph(view.getModel(), negativeSampleBugLocations));
		}
	}

	/**
	 * @param bugLocations           P bug locations from the positive sample.
	 * @param samplesPerLocation     Generate N negative samples for each given P.
	 * @param testSamplesPerLocation Generate M >= N samples and select the N most
	 *                               dissimilar samples in comparison to P.
	 * @param maxSamplesPerBug       Limit the number of negative samples min(L, N*P).
	 * @param minSampleDistance      Minimum distance to walk through the AST while
	 *                               generating negative samples.
	 * @param maxSampleDistance      Maximum distance to walk through the AST while
	 *                               generating negative samples.
	 * @return The min(L, N*P) negative samples.
	 */
	public Set<EObject> selectNegativeSamples(Set<EObject> bugLocations, 
			int samplesPerLocation, int testSamplesPerLocation, int maxSamplesPerBug, 
			int minSampleDistance, int maxSampleDistance) {
		
		List<EObject> negativeSamples = new ArrayList<>();
		
		for (EObject bugLocation : bugLocations) {
			EObject model = getModel(bugLocation);
			List<EObject> testSamples = selectRandomSample(model, bugLocation.eClass(), 
					testSamplesPerLocation, minSampleDistance, maxSampleDistance);
			Map<EObject, Integer> rankedSamples = new HashMap<>();
			
			// Calculate similarity between original bug location and the negative samples:
			for (EObject testSample : testSamples) {
				int similarity = calculateSimilarity(bugLocation, testSample);
				rankedSamples.put(testSample, similarity);
			}
			
			// Take the most dissimilar model elements as negative samples:
			Collections.shuffle(testSamples);
			Collections.sort(testSamples, (a, b) -> rankedSamples.get(a).compareTo(rankedSamples.get(b)));
			negativeSamples.addAll(testSamples.subList(0, Math.min(samplesPerLocation, testSamples.size())));
		}
		
		Collections.shuffle(negativeSamples);
		Set<EObject> selectedNegativeSamples = new HashSet<>();
		
		for (EObject negativeSample : negativeSamples) {
			if (selectedNegativeSamples.size() < maxSamplesPerBug) {
				selectedNegativeSamples.add(negativeSample);
			}
		}
		
		return selectedNegativeSamples;
	}

	private EObject getModel(EObject bugLocation) {
		EObject child = bugLocation;
		EObject parent = child.eContainer();
		
		while (parent != null) {
			child = parent;
			parent = child.eContainer();
		}
		
		return child; // top most parent
	}
	
	private List<EObject> selectRandomSample(EObject root, EClass type, 
			int count, int minSampleDistance, int maxSampleDistance) {
		List<EObject> randomSamples = new ArrayList<>(count);

		Iterator<EObject> modelIterator = root.eAllContents();
		int nextPosition = ThreadLocalRandom.current().nextInt(minSampleDistance, maxSampleDistance);
		int currentPosition = 0;
		boolean searchWithRestart = false;

		while ((count > 0) && modelIterator.hasNext()) {
			EObject element = modelIterator.next();
			++currentPosition;

			// Position reached?
			if (currentPosition >= nextPosition) {
				if (element.eClass() == type) {
					randomSamples.add(element);
					--count;
					nextPosition = ThreadLocalRandom.current().nextInt(minSampleDistance, maxSampleDistance);
					currentPosition = 0;
					searchWithRestart = false;
				}
			}

			// Restart model iteration?
			if (!modelIterator.hasNext()) {
				if (!searchWithRestart) {
					modelIterator = root.eAllContents();
				}

				// Search only only once through the whole model for a model element of specific type!
				if (currentPosition >= nextPosition) {
					searchWithRestart = true; 
				}
			}
		}
		
		return randomSamples;
	}
	
	private int calculateSimilarity(EObject modelElementA, EObject modelElementB) {
		String labelA = ModelUtil.getLabel(modelElementA);
		List<String> adjacentLabelsA = getAdjacentLabels(modelElementA);
		
		String labelB = ModelUtil.getLabel(modelElementB);
		List<String> adjacentLabelsB = getAdjacentLabels(modelElementB);
		
		int similarity = calculateSimilarity(labelA, labelB);
		
		for (String adjacentLabelA : adjacentLabelsA) {
			for (String adjacentLabelB : adjacentLabelsB) {
				int adjacentSimilarity = calculateSimilarity(adjacentLabelA, adjacentLabelB);
				
				if (adjacentSimilarity > 0) {
					similarity += adjacentSimilarity;
					break; // greedy match elements
				}
			}
		}
		
		return similarity;
	}
	
	private int calculateSimilarity(String modelElementA, String modelElementB) {
		if (modelElementA.equals(modelElementB)) {
			return 2;
		} else if (modelElementA.contains(modelElementB) || modelElementB.contains(modelElementA)) {
			return 1;
		} else {
			return 0;
		}
	}

	private List<String> getAdjacentLabels(EObject modelElement) {
		List<String> adjacentLabels = new ArrayList<>();
		String containerLabel = ModelUtil.getLabel(modelElement.eContainer());
		
		if ((containerLabel != null) && !containerLabel.isEmpty()) {
			adjacentLabels.add(containerLabel);
		}
		
		for (EReference reference : modelElement.eClass().getEAllReferences()) {
			if (reference.isMany()) {
				for (Object targetElement : (Iterable<?>) modelElement.eGet(reference)) {
					String targetLabel = ModelUtil.getLabel((EObject) targetElement);
					
					if ((targetLabel != null) && !targetLabel.isEmpty()) {
						adjacentLabels.add(targetLabel);
					}
				}
			} else {
				Object targetElement = modelElement.eGet(reference);
				
				if (targetElement != null) {
					String targetLabel = ModelUtil.getLabel((EObject) targetElement);
					
					if ((targetLabel != null) && !targetLabel.isEmpty()) {
						adjacentLabels.add(targetLabel);
					}
				}
			}
		}
		
		return adjacentLabels;
	}

	protected Set<EObject> createLocalBugLocalizationGraph(EObject model, Set<EObject> locations) {
		Set<EObject> modelElements = new LinkedHashSet<>();
		Set<EObject> childTree = new LinkedHashSet<>();
		
		// Locations:
		if (locations.remove(null)) {
			System.err.println("Removing Null Loaction!");
		}
		modelElements.addAll(locations);

		/*
		 *  Parents/Container Elements:
		 */
		
		for (EObject location : locations) {
			KNeighbourSettings kNeighbourSettings = type2kNeighbourSettings.get(location.eClass());
			
			if (kNeighbourSettings != null) {
				ModelUtil.collectParents(modelElements, location, kNeighbourSettings.parentLevels);
			} else {
				modelElements.add(location);
			}
		}
		
		/*
		 * Child Elements:
		 */
		
		for (EObject location : locations) {
			KNeighbourSettings kNeighbourSettings = type2kNeighbourSettings.get(location.eClass());
			
			if (kNeighbourSettings != null) {
				ModelUtil.collectOutgoingReferences(childTree, location, 
						kNeighbourSettings.childLevels,
						e -> e.isContainment());
			} else {
				modelElements.add(location);
			}
		}
		
		modelElements.addAll(childTree);
		
		/*
		 * Outgoing Elements of Child Tree:
		 */
		
		for (EObject modelElement : childTree) {
			KNeighbourSettings kNeighbourSettings = type2kNeighbourSettings.get(modelElement.eClass());
			
			if (kNeighbourSettings != null) {
				ModelUtil.collectOutgoingReferences(modelElements, modelElement, 
						kNeighbourSettings.outgoingDistance, 
						e -> (!e.isContainment() && !e.isContainer()));
			} else {
				modelElements.add(modelElement);
			}
		}
		
		/*
		 * Incoming Elements of Child Tree (within Model):
		 */
		
		modelElements.addAll(ModelUtil.collectIncomingReferences(model, childTree, 
				(type) -> {
					KNeighbourSettings kNeighbourSettings = type2kNeighbourSettings.get(type);
					
					if (kNeighbourSettings != null) {
						return kNeighbourSettings.incomingDistance;
					}
					
					return 0;
				},
				e -> (!e.isContainment() && !e.isContainer())));
		
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

	public Map<EClass, KNeighbourSettings> getType2kNeighbourSettings() {
		return type2kNeighbourSettings;
	}

	public void setType2kNeighbourSettings(Map<EClass, KNeighbourSettings> type2kNeighbourSettings) {
		this.type2kNeighbourSettings = type2kNeighbourSettings;
	}
}
