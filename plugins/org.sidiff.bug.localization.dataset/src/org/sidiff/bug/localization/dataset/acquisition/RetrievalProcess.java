package org.sidiff.bug.localization.dataset.acquisition;

import org.sidiff.bug.localization.dataset.configuration.RetrievalConfiguration;
import org.sidiff.bug.localization.dataset.model.DataSet;

public class RetrievalProcess {
	
	private RetrievalConfiguration configuration; 

	private DataSet dataset;

	public RetrievalProcess(RetrievalConfiguration configuration, DataSet dataset) {
		this.configuration = configuration;
		this.dataset = dataset;
	}
	
	public void process() {
		// TODO
		System.out.println(configuration);
		System.out.println(dataset);
	}
}
