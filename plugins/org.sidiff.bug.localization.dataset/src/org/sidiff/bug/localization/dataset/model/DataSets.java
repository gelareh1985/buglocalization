package org.sidiff.bug.localization.dataset.model;

import java.util.List;

public class DataSets {

	private List<DataSet> dataSets;

	public List<DataSet> getDataSets() {
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
