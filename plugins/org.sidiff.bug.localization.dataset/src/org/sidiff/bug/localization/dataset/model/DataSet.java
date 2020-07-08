package org.sidiff.bug.localization.dataset.model;

public class DataSet {

	private String repositoryURL;
	
	private String repositoryName;
	
	public DataSet() {
	}
	
	public DataSet(String repositoryURL) {
		this.repositoryURL = repositoryURL;
		
		// cut '.git' from last URL path segment:
		this.repositoryName = repositoryURL.substring(
				repositoryURL.lastIndexOf("/") + 1,
				repositoryURL.lastIndexOf("."));
	}

	public String getRepositoryURL() {
		return repositoryURL;
	}

	public void setRepositoryURL(String repositoryURL) {
		this.repositoryURL = repositoryURL;
	}

	public String getRepositoryName() {
		return repositoryName;
	}

	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}

	@Override
	public String toString() {
		return "DataSet [repositoryURL=" + repositoryURL + ", repositoryName=" + repositoryName + "]";
	}
}
