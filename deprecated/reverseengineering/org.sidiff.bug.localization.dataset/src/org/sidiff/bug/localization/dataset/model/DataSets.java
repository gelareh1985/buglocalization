package org.sidiff.bug.localization.dataset.model;

import java.util.Collections;
import java.util.List;

public class DataSets {

	private List<DataSet> dataSets;

	public List<DataSet> getDataSets() {
		
		if (dataSets == null) {
			return Collections.emptyList();
		}
		
		return dataSets;
	}

	public void setDataSets(List<DataSet> dataSets) {
		this.dataSets = dataSets;
	}

	@Override
	public String toString() {
		return "DataSets [dataSets=" + dataSets + "]";
	}
}
