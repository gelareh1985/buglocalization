package org.sidiff.bug.localization.dataset.history.repository;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Iterator;
import java.util.logging.Level;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.sidiff.bug.localization.dataset.history.Activator;
import org.sidiff.bug.localization.dataset.history.model.History;
import org.sidiff.bug.localization.dataset.history.model.Version;
import org.sidiff.bug.localization.dataset.history.repository.filter.VersionFilter;

public class GitRepository implements Repository {
	
	// https://www.vogella.com/tutorials/JGit/article.html
	// https://maximilian-boehm.com/en-gb/blog/use-java-library-jgit-to-programmatically-access-your-git-respository-2103371/
	
	private File localRepository;
	
	public GitRepository(File localRepository) {
		this.localRepository = localRepository;
	}
	
	public boolean exists() {
		return new File(localRepository.getAbsolutePath() + "/.git").exists();
	}
	
	@Override
	public History getHistory(VersionFilter filter) {
		History history = new History();
		
		if(!localRepository.exists()) {
			Activator.getLogger().log(Level.SEVERE, "Repository " + localRepository.getAbsolutePath() + " doesn't exist.");
			return history;
		} 

		try (Git git = openGitRepository()) {
			history.setIdentification(git.getRepository().getFullBranch());
			LogCommand logCommand = git.log().add(git.getRepository().resolve(Constants.HEAD));

			for (RevCommit revCommit : logCommand.call()) {
				String url = revCommit.getId().getName();
				String author = revCommit.getAuthorIdent().getName();
				String commitMessage = revCommit.getFullMessage();
				Instant date = Instant.ofEpochSecond(revCommit.getCommitTime());

				if (!filter.filter(url, date, author, commitMessage)) {
					Version version = new Version(url, date, author, commitMessage);
					history.getVersions().add(version);
				}
			}
        } catch (RevisionSyntaxException | IOException | GitAPIException e) {
        	Activator.getLogger().log(Level.SEVERE, "Exception occurred while reading repository history", e);
			e.printStackTrace();
		}

        return history;
	}

	public void clone(String repositoryURL) {
		try {
			Activator.getLogger().log(Level.INFO, "Cloning " + repositoryURL + " into " + localRepository);
			
			Git.cloneRepository().setURI(repositoryURL).setDirectory(localRepository).call();
		    
		    Activator.getLogger().log(Level.INFO, "Completed Cloning");
		} catch (GitAPIException e) {
			Activator.getLogger().log(Level.SEVERE, "Exception occurred while cloning repository", e);
		    e.printStackTrace();
		}
	}
	
	public void unlock() {
		File lockFile = new File(localRepository.getAbsoluteFile() + "/.git/index.lock");
		
		if (lockFile.exists()) {
			lockFile.delete();
		}
	}
	
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
	
	private String getCurrentVersionID(Git git) throws GitAPIException, IOException  {
		Iterator<RevCommit> revIterator = git.log().add(git.getRepository().resolve(Constants.HEAD)).call().iterator();
		
		if (revIterator.hasNext()) {
			return revIterator.next().getId().getName();
		}
		
		return null;
	}
	
	private Git openGitRepository() throws IOException {
		return new Git(new FileRepositoryBuilder().findGitDir(localRepository).build());
	}
}
