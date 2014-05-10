package com.emprovise.api.scm;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;

/**
 * Generic Scm Utility class to support various Source code management systems.
 * Currently the ScmUtil class supports implementation of Hg and Git. It provides
 * utility methods for cloning, getting repository remote url and determining the
 * type of local repository. It also defines the methods required to be implemented 
 * for supporting a scm system.
 * 
 * @author Pranav Patil
 *
 */
public abstract class ScmUtil {

	/**
	 * The minimum length of the mercurial changeset.
	 */
	public static final int HG_REV_LENGTH = 12;
	public static final int GIT_REV_LENGTH = Constants.OBJECT_ID_STRING_LENGTH;
	private static final String HEAD_REF = "refs" + File.separator + "heads" + File.separator;
	
	/**
	 * Pulls all the recent changes from the Repository. 
	 * @return String log
	 * 		{@link String} containing number of changesets pulled from remote repository. 
	 * @throws Exception
	 */
	public abstract String pull() throws Exception;
	public abstract String update(String startPoint, boolean doClean) throws Exception;
	public abstract List<String> getBranches() throws IOException;
	public abstract String getCurrentChangeSet() throws IOException;
	public abstract void tag(String user, String tagName) throws Exception;
	public abstract List<String> getAllTags();
	public abstract String getTagChangeset(String tagname);
	public abstract List<String> push() throws Exception;
	public abstract void revert(File... files) throws Exception;
	public abstract void close();
	public abstract String getFile(String filename, String revision) throws Exception;
	public abstract List<ScmRevision> getParentChangesets(String changeset) throws Exception;
	public abstract void revertAllTags() throws IOException;
	
	public static ScmUtil loadScmRepository(File repository) throws IOException {
		
		ScmType scmType = getScmType(repository);
		
		if(scmType.equals(ScmType.HG)) {
			return new HgUtil(repository);
		}
		else if(scmType.equals(ScmType.GIT)) {
			return new GitUtil(repository);
		}
		else {
			throw new RuntimeException("Invalid Scm Repository. Currently only Mercurial and Git source control are supported.");
		}
	}

	
	public static ScmUtil loadScmRepository(File repository, String username, String password) throws IOException {
		
		ScmType scmType = getScmType(repository);
		
		if(scmType.equals(ScmType.HG)) {
			String scmUrl = getScmUrl(repository, scmType);
			return new HgUtil(repository, username, password, scmUrl);
		}
		else if(scmType.equals(ScmType.GIT)) {
			return new GitUtil(repository, username, password, null);
		}
		else {
			throw new UnsupportedOperationException("Only Scm.HG type is supported for this method.");
		}
	}
	
	public static ScmUtil cloneScmRepository(ScmType scmType, String scmUrl, String repository) throws InterruptedException, IOException, GitAPIException {
		
		if(scmType.equals(ScmType.HG)) {
			return HgUtil.clone(scmUrl, repository);
		}
		else if(scmType.equals(ScmType.GIT)) {
			return GitUtil.clone(scmUrl, repository);
		}
		else {
			throw new RuntimeException("Invalid Scm Repository. Currently only Mercurial and Git source control are supported.");
		}
	}
	
	public static ScmUtil cloneScmRepository(ScmType scmType, String scmUrl, String repository, String username, String password) throws IOException, GitAPIException {
		
		if(scmType.equals(ScmType.HG)) {
			return HgUtil.clone(scmUrl, repository, username, password);
		}
		else if(scmType.equals(ScmType.GIT)) {
			return GitUtil.clone(scmUrl, repository, username, password);
		}
		else {
			throw new RuntimeException("Invalid Scm Repository. Currently only Mercurial and Git source control are supported.");
		}
	}
	
	public static String getScmUrl(File repository) throws IOException {
		ScmType scmType = getScmType(repository);
		return getScmUrl(repository, scmType);
	}

	public static String getScmCommitUrl(File repository) throws IOException {
		
		ScmType scmType = getScmType(repository);
		String scmUrl = getScmUrl(repository, scmType);
		
		if(scmType.equals(ScmType.HG)) {

			if(!scmUrl.endsWith("/")) {
   		 		scmUrl = scmUrl + "/";
   		 	}

			return scmUrl + "rev/"; 
		}
		else {
			return scmUrl + "commit/";
		}
	}

	public static File getScmRepository(File directory) throws IOException {
		File hgRepo = new File(directory, File.separator + ".hg");
		File gitRepo = new File(directory, File.separator + ".git");
		
		if(hgRepo.exists() && hgRepo.isDirectory()) {
			return hgRepo;
		}
		else if(gitRepo.exists() && gitRepo.isDirectory()) {
			return gitRepo;
		}
		else {
			return null;
		}
	}
	
	public static ScmType getScmType(File repository) {
		File hgRepo = new File(repository, ".hg");
		File gitRepo = new File(repository, ".git");
		
		if(hgRepo.exists() && hgRepo.isDirectory()) {
			return ScmType.HG;
		}
		else if(gitRepo.exists() && gitRepo.isDirectory()) {
			return ScmType.GIT;
		}
		else {
			throw new RuntimeException("Invalid Scm Repository. Currently only Mercurial and Git source control are supported.");
		}
	}
	
	
	/**
	 * Reads the default remote repository url from the hgrc file in the .hg directory.
	 * @param repoDir
	 * 		{@link File} representing the .hg directory of the current project.
	 * @return
	 * 		{@link String} containing the remote mercurial repository url. 
	 * @throws IOException
	 */
	public static String getScmUrl(File repository, ScmType scmType) throws IOException {
		
		if(scmType.equals(ScmType.HG)) {
			File hgrc = new File(repository, ".hg" + File.separator + "hgrc");
			String scmUrl = getPropertyValue(hgrc, "default");
			return scmUrl;			
		}
		else if(scmType.equals(ScmType.GIT)) {
			File gitConfig = new File(repository, ".git" + File.separator + "config");
			String scmUrl = getPropertyValue(gitConfig, "url");
			scmUrl = scmUrl.replace(":", "/");
			scmUrl = scmUrl.replace("git@", "https://");
			scmUrl = scmUrl.replace(".git", "/");
			return scmUrl;			
		}
		else {
			throw new RuntimeException("Invalid Scm Repository. Currently only Mercurial and Git source control are supported.");
		}
	}
	
	public static String getCurrentBranchName(File repository) throws IOException {
		ScmType scmType = getScmType(repository);
		return getCurrentBranchName(repository, scmType);
	}
	
	public static String getCurrentBranchName(File repository, ScmType scmType) throws IOException {
		
		if(scmType.equals(ScmType.HG)) {
			File hgBranchFile = new File(repository, ".hg" + File.separator + "branch");
			
			if(hgBranchFile.exists()) {
				List<String> lines = FileUtils.readLines(new File(hgBranchFile.getAbsolutePath()), "UTF-8"); 
					
				if(lines != null && !lines.isEmpty()) {
					return lines.get(0);
				}
			}
		}
		else if(scmType.equals(ScmType.GIT)) {
			File gitBranchFile = new File(repository, ".git" + File.separator + "HEAD");
			
			if(gitBranchFile.exists()) {
				List<String> lines = FileUtils.readLines(new File(gitBranchFile.getAbsolutePath()), "UTF-8");
					
				if(lines != null && !lines.isEmpty()) {
					String head = lines.get(0);
					return head.substring(head.indexOf(HEAD_REF) + HEAD_REF.length());
				}
			}
		}
		
		return null;
	}
	
	private static String getPropertyValue(File txtFile, String parameter) throws IOException {
		
		String value = null;
		List<String> lines = FileUtils.readLines(txtFile, "UTF-8");
			
		for (String line : lines) {
			if(line.contains(parameter)) {
				value = line.substring(line.indexOf(parameter) + parameter.length());
				value = value.replaceAll("[=]", "").trim();
				break;
			}
		}
		
		return value;
	}
}
