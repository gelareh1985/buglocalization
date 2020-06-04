package org.sidiff.bug.localization.retrieval.reports;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.sidiff.bug.localization.retrieval.reports.bugtracker.BugzillaBugtracker;
import org.sidiff.bug.localization.retrieval.reports.bugtracker.EclipseBugzillaBugtracker;
import org.sidiff.bug.localization.retrieval.reports.model.BugReport;
import org.sidiff.bug.localization.retrieval.reports.util.JsonUtil;

import com.google.gson.JsonElement;

public class Application implements IApplication {

	@Override
	public Object start(IApplicationContext context) throws Exception {

		int bugID = 2000;
		BugzillaBugtracker bugtracker = new EclipseBugzillaBugtracker();

		// Bug JSON:
		System.out.println("\n#################### JSON Bug Report ####################\n");
		
		JsonElement jsonElementBug = bugtracker.getBugJSON(bugID);

		System.out.println(JsonUtil.print(jsonElementBug));
		System.out.println();
		
		// Comment JSON:
		System.out.println("\n#################### JSON Bug Report Comments ####################\n");
		
		JsonElement jsonElementComments = bugtracker.getCommentsJSON(bugID);
		
		System.out.println(JsonUtil.print(jsonElementComments));
		
		// Bug Report:
		System.out.println("\n#################### Java Bug Report ####################\n");
		
		BugReport bugReport = bugtracker.getBugReport(bugID);
		System.out.println(bugReport);
		
		System.out.println("\n#################### Java Bug Report Comments ####################\n");
		
		bugReport.getComments().forEach(System.out::println);

		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
	}

}
