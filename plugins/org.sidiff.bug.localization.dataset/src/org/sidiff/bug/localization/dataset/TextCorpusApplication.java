package org.sidiff.bug.localization.dataset;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.sidiff.bug.localization.common.utilities.web.WebUtil;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.model.DataSet;
import org.sidiff.bug.localization.dataset.model.util.DataSetStorage;
import org.sidiff.bug.localization.dataset.reports.model.BugReportComment;

public class TextCorpusApplication implements IApplication {

	public static final String ARGUMENT_DATASET = "-dataset";
	
	
	@Override
	public Object start(IApplicationContext context) throws Exception {

		Path dataSetPath = getPathFromProgramArguments(context, ARGUMENT_DATASET);
		DataSet dataSet = DataSetStorage.load(dataSetPath);
		
		// Build text corpus:
		StringBuilder textCorpus = new StringBuilder();
		
		for (Version version : dataSet.getHistory().getVersions()) {
			if (version.hasBugReport()) {
				textCorpus.append(getPlainText(version.getCommitMessage()));
				textCorpus.append(" ");
				textCorpus.append(getPlainText(version.getBugReport().getSummary()));
				textCorpus.append(" ");
				
				for (BugReportComment comment : version.getBugReport().getComments()) {
					if (!comment.getText().contains("Gerrit change")) {
						textCorpus.append(getPlainText(comment.getText()));
						textCorpus.append(" ");
					}
				}
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

	private String getPlainText(String string) {
		return WebUtil.unescape(string).replace("\n", "").replace("\r", "");
	}

	@Override
	public void stop() {
	}

	private Path getPathFromProgramArguments(IApplicationContext context, String argumentName) throws FileNotFoundException {
		String[] args = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		
		for (int i = 0; i < args.length - 1; i++) {
			if (args[i].equals(argumentName)) {
				Path dataSetPath = Paths.get(args[i + 1]);
				
				if (!Files.exists(dataSetPath)) {
					throw new FileNotFoundException(args[i + 1]);
				}
				
				return dataSetPath;
			}
		}
		
		throw new FileNotFoundException("Program argument '" + argumentName + "' not specified.");
	}
	
}
