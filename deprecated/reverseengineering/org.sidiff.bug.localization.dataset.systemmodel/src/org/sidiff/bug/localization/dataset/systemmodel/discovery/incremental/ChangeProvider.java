package org.sidiff.bug.localization.dataset.systemmodel.discovery.incremental;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

public interface ChangeProvider {

	Set<IPath> getChanges(IProject project);
}
