package org.sidiff.bug.localization.dataset;

import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.model.util.DataSetStorage;
import org.sidiff.bug.localization.dataset.retrieval.util.ApplicationUtil;
import org.sidiff.bug.localization.dataset.retrieval.util.BugReportUtil;

/**
 * Retrieves a text corpus file from all bug reports of data set.
 */
public class TextCorpusApplication implements IApplication {

	public static final String ARGUMENT_DATASET = "-dataset";
	
	@Override
	public Object start(IApplicationContext context) throws Exception {

		Path dataSetPath = ApplicationUtil.getPathFromProgramArguments(context, ARGUMENT_DATASET);
		DataSet dataSet = DataSetStorage.load(dataSetPath);
		
		// Build text corpus:
		StringBuilder textCorpus = new StringBuilder();
		
		for (Version version : dataSet.getHistory().getVersions()) {
			if (version.hasBugReport()) {
				textCorpus.append(BugReportUtil.getFullPlainText(version, BugReportUtil.DEFAULT_BUG_REPORT_COMMENT_FILTER));
				textCorpus.append("\n");
			}
		}
		
		// Save text corpus to file:
		String textCorpusFileName = dataSetPath.getFileName().toString();
		textCorpusFileName = textCorpusFileName.substring(0, textCorpusFileName.lastIndexOf("."));
		textCorpusFileName = textCorpusFileName + "_corpus.txt"; 
		
		Path textCorpusPath = dataSetPath.getParent().resolve(textCorpusFileName);
		
		Files.write(textCorpusPath, textCorpus.toString().getBytes());
		
		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
	}
	
}
