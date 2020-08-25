package org.sidiff.bug.localization.dataset.history.repository;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.Side;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.sidiff.bug.localization.dataset.history.Activator;
import org.sidiff.bug.localization.dataset.history.model.History;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.model.changes.FileChange;
import org.sidiff.bug.localization.dataset.history.model.changes.LineChange;
import org.sidiff.bug.localization.dataset.history.model.changes.FileChange.FileChangeType;
import org.sidiff.bug.localization.dataset.history.model.changes.LineChange.LineChangeType;
import org.sidiff.bug.localization.dataset.history.repository.filter.VersionFilter;

public class GitRepository implements Repository {
	
	// https://www.vogella.com/tutorials/JGit/article.html
	// https://maximilian-boehm.com/en-gb/blog/use-java-library-jgit-to-programmatically-access-your-git-respository-2103371/
	
	private String repositoryURL;
	
	private File workingDirectory;
	
	public GitRepository(String repositoryURL, File workingDirectory) {
		this.repositoryURL = repositoryURL;
		this.workingDirectory = workingDirectory;
	}
	
	public GitRepository(File localRepository) {
		this.workingDirectory = localRepository;
	}
	
	@Override
	public Path getWorkingDirectory() {
		return Paths.get(workingDirectory.getPath());
	}
	
	public boolean exists() {
		return new File(workingDirectory.getAbsolutePath() + "/.git").exists();
	}
	
	private Git openGitRepository() throws IOException {
		
		if(!exists()) {
			if(repositoryURL != null) {
				Activator.getLogger().log(Level.INFO, "Repository " + repositoryURL + " will be cloned.");
				return cloneGit();
			} else {
				Activator.getLogger().log(Level.INFO, "A new repository " + workingDirectory.getAbsolutePath() + " will be created.");
				return createGit();
			}
		}
		
		return new Git(new FileRepositoryBuilder().findGitDir(workingDirectory).build());
	}
	
	@Override
	public History getHistory(VersionFilter filter) {
		History history = new History();
		
		try (Git git = openGitRepository()) {
			history.setIdentification(git.getRepository().getFullBranch());
			LogCommand logCommand = git.log().add(git.getRepository().resolve(Constants.HEAD));
			
			boolean retainNextVersion = false; // retain version after none filtered?

			for (RevCommit revCommit : logCommand.call()) {
				String id = revCommit.getId().getName();
				String author = revCommit.getAuthorIdent().getName();
				String commitMessage = revCommit.getFullMessage();
				Instant date = Instant.ofEpochSecond(revCommit.getCommitTime());
				
				boolean isFilteredVersion = filter.filter(id, date, author, commitMessage);

				if (retainNextVersion || !isFilteredVersion) {
					Version version = new Version(id, date, author, commitMessage);
					version.setVisible(!isFilteredVersion);
					history.getVersions().add(version);
				}
				
				retainNextVersion = !isFilteredVersion && filter.retainRevisions();
			}
        } catch (RevisionSyntaxException | IOException | GitAPIException e) {
        	Activator.getLogger().log(Level.SEVERE, "Exception occurred while reading repository history", e);
			e.printStackTrace();
		}

        return history;
	}

	public Git cloneGit() {
		try {
			Activator.getLogger().log(Level.INFO, "Cloning " + repositoryURL + " into " + workingDirectory);
			Git git = Git.cloneRepository().setURI(repositoryURL).setDirectory(workingDirectory).call();
		    Activator.getLogger().log(Level.INFO, "Completed Cloning");
		    return git;
		} catch (GitAPIException e) {
			Activator.getLogger().log(Level.SEVERE, "Exception occurred while cloning repository", e);
		    e.printStackTrace();
		}
		
		return null;
	}
	
	public Git createGit() {
		try {
			return Git.init().setDirectory(workingDirectory).call();
		} catch (IllegalStateException | GitAPIException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public boolean checkout(History history, Version version) {
		
		try (Git git = openGitRepository()) {
			
			// Unlock repository (if necessary):
			unlock();
			
			// Clean up current branch (if necessary):
			git.reset().setMode(ResetType.HARD).call();
			git.clean().setCleanDirectories(true).call();
			
			// Switch to requested branch (if necessary):
			git.checkout().setCreateBranch(false).setName(history.getIdentification())
					.setStartPoint(version.getIdentification()).setForceRefUpdate(true).call();

			// Check out specific version:
			git.reset().setRef(version.getIdentification()).call();
			
			// Clean up current version (if necessary):
			git.reset().setRef(version.getIdentification()).setMode(ResetType.HARD).call();
			git.clean().setCleanDirectories(true).call();
			
			if (version.getIdentification().equals(getCurrentVersionID(git))) {
				return true;
			}
		} catch (IOException | GitAPIException e) {
			e.printStackTrace();
		}
		
		Activator.getLogger().log(Level.SEVERE, "Could not check out version=" + version + ", History=" + history);
		return false;
	}
	
	public void unlock() {
		File lockFile = new File(workingDirectory.getAbsoluteFile() + "/.git/index.lock");
		
		if (lockFile.exists()) {
			lockFile.delete();
		}
	}

	private String getCurrentVersionID(Git git) throws GitAPIException, IOException  {
		Iterator<RevCommit> revIterator = git.log().add(git.getRepository().resolve(Constants.HEAD)).call().iterator();
		
		if (revIterator.hasNext()) {
			return revIterator.next().getId().getName();
		}
		
		return null;
	}
	
	@Override
	public String commit(String authorName, String authorEmail, String message, String username, String password) {
		try (Git git = openGitRepository()) {
			git.add().addFilepattern(".").call();
			RevCommit commit = git.commit().setAll(true).setAllowEmpty(true)
					.setAuthor(authorName, authorEmail)
					.setCommitter(authorName, authorEmail)
					.setMessage(message).call();

			if ((repositoryURL != null) && (username != null) && (password != null)) {
				PushCommand pushCommand = git.push();
			    pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password));
			    pushCommand.call();
			}
			
			return commit.getId().getName();
		} catch (IOException | GitAPIException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public List<FileChange> getChanges(Version version) {
		
		try (Git git = openGitRepository()) {
			ObjectId oldHead = git.getRepository().resolve(version.getIdentification() + "~1^{tree}");
            ObjectId head = git.getRepository().resolve(version.getIdentification() + "^{tree}");

    		try (ObjectReader reader = git.getRepository().newObjectReader()) {
        		CanonicalTreeParser oldTreeIterator = new CanonicalTreeParser();
        		oldTreeIterator.reset(reader, oldHead);
        		CanonicalTreeParser newTreeIterator = new CanonicalTreeParser();
        		newTreeIterator.reset(reader, head);

        		OutputStream outputStream = DisabledOutputStream.INSTANCE;
        		
				try (DiffFormatter formatter = new DiffFormatter(outputStream)) {
					formatter.setRepository(git.getRepository());
					List<DiffEntry> diffs = formatter.scan(oldTreeIterator, newTreeIterator);
					List<FileChange> fileChanges = new ArrayList<>(diffs.size());

					for (DiffEntry entry : diffs) {
						FileHeader fileHeader = formatter.toFileHeader(entry);

						// changed file:
						FileChange fileChange = new FileChange();
						fileChange.setLocation(Paths.get(entry.getPath(Side.OLD)));
						fileChange.setType(FileChangeType.valueOf(entry.getChangeType().toString()));
						fileChanges.add(fileChange);
						
						// changed lines:
						for (Edit edit : fileHeader.toEditList()) {
							LineChange lineChange = new LineChange();
							lineChange.setType(LineChangeType.valueOf(edit.getType().toString()));
							
							lineChange.setBeginA(edit.getBeginA());
							lineChange.setEndA(edit.getEndA());
							lineChange.setBeginB(edit.getBeginB());
							lineChange.setEndB(edit.getEndB());
							
							fileChange.getChanges().add(lineChange);
						}
					}

					return fileChanges;
				}
    		} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (IOException | RevisionSyntaxException e1) {
			e1.printStackTrace();
		}
		
		return Collections.emptyList();
	}
}
