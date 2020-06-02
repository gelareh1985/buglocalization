package org.sidiff.bug.localization.retrieval.history.model.util.test;

import static org.junit.Assert.*;
import org.junit.Test;
import org.sidiff.bug.localization.retrieval.history.model.util.BugFixVersionFilter;


public class BugFixFilterTest {

	// bug: "bug.*?\\d"
	
	@Test
	public void testStartsWithBugAndNumber() {
		assertTrue(isNotFiltered("Bug 563546 - Regression test for failure"));
	}
	
	@Test
	public void testContainsBugAndNumber() {
		assertTrue(isNotFiltered("revised for bug 519417[completion] should propose types & packagesexported from required modules"));
	}
	
	// bug: ".*?\\dbug"
	
	@Test
	public void testStartsWithNumberAndBug() {
		assertTrue(isNotFiltered("563546 Bug - Regression test for failure"));
	}
	
	@Test
	public void testContainsNumberAndBug() {
		assertTrue(isNotFiltered("revised for 519417 bug[completion] should propose types & packagesexported from required modules"));
	}
	
	// fix: "fix.*?\\d"
	
	@Test
	public void testStartsWithFixAndNumber() {
		assertTrue(isNotFiltered("Fix 563546 - Regression test for failure"));
	}
	
	@Test
	public void testContainsFixAndNumber() {
		assertTrue(isNotFiltered("[completion] should propose types & packagesexported from required modules - fix for 519417 "));
	}
	
	// fix: ".*?\\dfix"
	
	@Test
	public void testStartsWithNumberAndFix() {
		assertTrue(isNotFiltered("563546 Fix - Regression test for failure"));
	}
	
	@Test
	public void testContainsNumberAndFix() {
		assertTrue(isNotFiltered("should propose types & packagesexported from required modules - 519417 fixed "));
	}
	
	// helper:
	
	public boolean isNotFiltered(String message) {
		BugFixVersionFilter bugFixFilterTest = new BugFixVersionFilter();
		return !bugFixFilterTest.filter("", null, "", message);
	}
}
