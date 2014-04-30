package com.emprovise.api.scm;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aragost.javahg.BaseRepository;
import com.aragost.javahg.Changeset;
import com.aragost.javahg.Repository;
import com.aragost.javahg.RepositoryConfiguration;
import com.aragost.javahg.commands.Branch;
import com.aragost.javahg.commands.BranchesCommand;
import com.aragost.javahg.commands.CatCommand;
import com.aragost.javahg.commands.LogCommand;
import com.aragost.javahg.commands.PullCommand;
import com.aragost.javahg.commands.PushCommand;
import com.aragost.javahg.commands.RevertCommand;
import com.aragost.javahg.commands.Tag;
import com.aragost.javahg.commands.TagCommand;
import com.aragost.javahg.commands.TagsCommand;
import com.aragost.javahg.commands.UpdateCommand;
import com.aragost.javahg.commands.UpdateResult;

/**
 * @author Pranav Patil
 * Utility to perform Mercurial operations using JavaHg API
 */
public class HgUtil extends ScmUtil {

	/**
	 * Repository instance which is used to perform all the Mercurial operations except cloning
	 */
	private Repository repository;

	/**
	 * Mercurial repository configuration containing authentication details with remote repository url. 
	 */
	private File hgrc;

	private static Logger log = LoggerFactory.getLogger(HgUtil.class);
	
	/**
	 * Creates a new HgUtil Object using the path to existing repository
	 * @param directoryPath
	 * 		The location to the Existing Repository
	 */
	public HgUtil(String directoryPath) {
		super();
		this.repository = Repository.open(new File(directoryPath));
	}

	/**
	 * Creates a new HgUtil Object using the specified directory containing the existing repository
	 * @param directory
	 * 		{@link File} representing the Existing Repository directory.
	 */
	public HgUtil(File directory) {
		super();
		this.repository = Repository.open(directory);
	}
	
	/**
	 * Creates a new HgUtil Object using the specified directory containing the existing repository.
	 * Also initializes the mercurial configuration file required for clone or pull operations using
	 * https or ssh connection.
	 * @param directory
	 * 		{@link File} representing the Existing Repository directory.
	 * @param hgrc
	 * 		A {@link File} object containing mercurial repository configuration.
	 */
	public HgUtil(File directory, File hgrc) {
		super();
		this.repository = Repository.open(directory);
		this.hgrc = hgrc;
	}

	/**
	 * Creates a new HgUtil Object using the specified directory containing the existing repository
	 * along with the provided authorization credentials. 
	 * @param directory
	 * 		{@link File} representing the Existing Repository directory.
	 * @param user
	 * 		{@link String} authorized user id.
	 * @param password
	 * 		{@link String} password for authentication.
	 * @param remoteUrl
	 * 		{@link String} URL to the remote mercurial repository.
	 * @throws IOException 
	 */
	public HgUtil(File directory, String user, String password, String remoteUrl) throws IOException {
		super();
		this.hgrc = File.createTempFile(".hgrc","");

		StringBuilder contents = new StringBuilder();
		contents.append("[paths] \n");
		contents.append("default = ").append(remoteUrl).append(" \n");
		contents.append("[auth] \n");
		contents.append("repo.prefix = ").append(remoteUrl).append(" \n");
		contents.append("repo.username = " + user + " \n");
		contents.append("repo.password = " + password + " \n");
		contents.append("repo.schemes = https \n");
		FileUtils.writeStringToFile(hgrc, contents.toString());
		
		RepositoryConfiguration repoConfig = new RepositoryConfiguration();
		repoConfig.setHgrcPath(hgrc.getAbsolutePath());
		
		this.repository = Repository.open(repoConfig, directory);
	}
	
	/**
	 * Creates a new HgUtil Object using a {@link Repository} object
	 * @param repository
	 * 		A {@link Repository} object which is created from existing repository path or 
	 * 		by using the {@link clone} method
	 * @param hgrc
	 * 		A {@link File} object containing mercurial repository configuration.
	 */
	public HgUtil(Repository repository, File hgrc) {
		super();
		this.repository = repository;
		this.hgrc = hgrc;
	}

	/**
	 * Pulls all the recent changes from the Repository. Note that if the remote repository requires
	 * authorization information, then mercurial configuration 'hgrc' must be initialized. If hgrc
	 * is not initialized the pull from secured repository will return 'no changes' regardless. 
	 * @return Output log
	 * 		{@link Output} containing number of changesets pulled from remote repository. 
	 * @throws IOException
	 */
	public String pull() throws Exception {
		
		String log = "no changes";
		List<Changeset> changesets = PullCommand.on(repository).execute();
		
		if(changesets!= null && !changesets.isEmpty()) {
			log = changesets.size() + " new changesets";
		}
		
		return log;
	}

	/**
	 * Updates the repository to the specified changeset
	 * @param node
	 * 		The change set or the node to update the codebase 
	 * @param doClean 
	 * 		It enables to remove all the uncommitted and unversioned files 
	 * @return Output log
	 * 		{@link Output} containing updated changeset. 
	 * @throws IOException
	 */
	public String update(String node, boolean doClean) throws IOException {
		StringBuilder log = new StringBuilder(); 
		Changeset changeset = new Changeset(repository, node);
		
		UpdateResult result = null;
		
		if(doClean) {
			result = UpdateCommand.on(repository).clean().rev(changeset).execute();
		}
		else {
			result = UpdateCommand.on(repository).rev(changeset).execute();
		}
		
        log.append("updated ").append(result.getUpdated()).append(", ");
        log.append("merged ").append(result.getMerged()).append(", ");
        log.append("removed ").append(result.getRemoved()).append(", ");
        log.append("unresolved ").append(result.getUnresolved()).append("\n");
        
        return log.toString();
	}

	/**
	 * Clones the specified remote repository to local repository at the specified local path
	 * @param remoteUrl
	 * 		Url to the remote mercurial repository
	 * @param repository
	 * 		Path to the local mercurial repository
	 * @return
	 * 		Status of the operation
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public static HgUtil clone(String remoteUrl, String repository) throws InterruptedException, IOException {

		Process proc = null;
		StringBuilder cloneRepo = new StringBuilder("hg clone ").append(remoteUrl).append(" ").append(repository);
		proc = Runtime.getRuntime().exec(cloneRepo.toString());
			proc.waitFor();
		
		if (proc.exitValue()==0){
    		return new HgUtil(repository);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Clones the specified remote repository to local repository at the specified local path using JavaHg API
	 * @param remoteUrl
	 * 		Url to the remote mercurial repository
	 * @param repository
	 * 		Path to the local mercurial repository 		
	 * @param user
	 * 		User id to authorize the access to secured repository 
	 * @param password
	 * 		Password to authenticate the access to the specified user id
	 * @return
	 * 		A {@link HgUtil} object with the cloned {@link Repository}
	 * @throws IOException
	 */
	public static HgUtil clone(String remoteUrl, String repository, String user, String password) throws IOException {

		File hgrc = File.createTempFile(".hgrc","");

		StringBuilder contents = new StringBuilder();
		contents.append("[paths] \n");
		contents.append("default = ").append(remoteUrl).append(" \n");
		contents.append("[auth] \n");
		contents.append("repo.prefix = ").append(remoteUrl).append(" \n");
		contents.append("repo.username = " + user + " \n");
		contents.append("repo.password = " + password + " \n");
		contents.append("repo.schemes = https \n");
		FileUtils.writeStringToFile(hgrc, contents.toString());
			
		RepositoryConfiguration repoConfig = new RepositoryConfiguration();
		repoConfig.setHgrcPath(hgrc.getAbsolutePath());
			
		BaseRepository repo = Repository.clone(repoConfig, new File(repository), remoteUrl);
		return new HgUtil(repo, hgrc);
	}
	
	/**
	 * Returns all the changesets for the specified branch name, from the specified revision number over a period
	 * of months specified.
	 * @param branch
	 * 		{@link Branch} for which the changesets should fetched.
	 * @param months
	 * 		Number of months for which the changesets should be fetched.
	 * @param rev
	 * 		Mercurial Revision number from which the changesets should be fetched.
	 * @return
	 * 		{@link List} of {@link Changeset} fetched from the repository 
	 */
	public List<Changeset> getChangeSets(String branch, int months, String rev) {
		
		LogCommand log = LogCommand.on(repository);

		if (months > 0) {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			GregorianCalendar startCal = new GregorianCalendar();
			startCal.add(Calendar.MONTH, -months);
			String start = dateFormat.format(startCal.getTime());
			String now = dateFormat.format(new GregorianCalendar().getTime());
			String argFormat = "%s to %s";
			log.date(format(argFormat, start, now));
		}

		if(rev != null) {
			log.rev(rev);
		}
		
		log.branch(branch);
		return log.execute();
	}

	/**
	 * Get the the minimum changeset in the specified mercurial branch.
	 * @param branch
	 * 		{@link Branch} for which the changesets should fetched.
	 * @return
	 * 		{@link Changeset} fetched from the repository
	 */
	public Changeset getFirstChangeSet(String branch) {
		
		String revision = "min(branch('" + branch + "'))";
		LogCommand log = LogCommand.on(repository);
		log.rev(revision);
		List<Changeset> changesets = log.execute();
		
		if(changesets != null && !changesets.isEmpty()) {
			return changesets.get(0);
		}
		else {
			return null;
		}
	}

	/**
	 * Get the parent changesets for the specified changeset.
	 * @param changeset
	 * 		{@link Changeset} for which the parent changesets should be fetched.
	 * @return
	 * 		{@link List} of {@link Changeset} fetched from the repository
	 */
	public List<ScmRevision> getParentChangesets(String changeset) {

		String revision = "parents(" + changeset + ")";
		LogCommand log = LogCommand.on(repository);
		log.rev(revision);
		List<Changeset> changesets = log.execute();
		List<ScmRevision> scmRevisions = new ArrayList<ScmRevision>();
		
		if(changesets == null) {
			return scmRevisions;
		}
		else {
			for (Changeset chset : changesets) {
				scmRevisions.add(new ScmRevision(chset));
			}
		}
		
		return scmRevisions;
	}
	
	/**
	 * Get all the change sets for the specified branch from the local repository. 
	 * @param branch
	 * 		{@link Branch} for which the changesets should fetched.
	 * @return
	 * 		{@link List} of {@link Changeset} fetched from the repository.
	 */
	public List<Changeset> getChangeSets(String branch) {
		
		LogCommand log = LogCommand.on(repository);
		log.branch(branch);
		return log.execute();
	}

	/**
	 * Gets the next changeset for the given branch from the specified revision number
	 * @param branch
	 * 		{@link Branch} for which the changesets should fetched.
	 * @param rev
	 * 		Revision number from which to get the next changeset.
	 * @return
	 * 		{@link Changeset} fetched from the repository.
	 */
	public Changeset getNextChangeSet(String branch, int rev) {
		
		LogCommand log = LogCommand.on(repository);
		log.branch(branch);
		log.limit(2);
		log.rev(String.valueOf(rev) + ":tip");
		List<Changeset> changesets = log.execute();
		
		if(changesets != null && changesets.size() == 2 ) {
			return changesets.get(1);
		}
		else {
			return  null;
		}
	}

	/**
	 * Gets all the changes for the specified branch between the specified range of revisions.
	 * @param branch
	 * 		{@link Branch} for which the changesets should fetched.
	 * @param fromRev
	 * 		Starting revision number.
	 * @param toRev
	 * 		Ending revision number.
	 * @return
	 * 		{@link List} of {@link Changeset} fetched from the repository.
	 */
	public List<Changeset> getChangeSets(String branch, int fromRev, int toRev) {
		
		LogCommand log = LogCommand.on(repository);
		log.branch(branch);
		log.rev(String.valueOf(fromRev) + ":" + String.valueOf(toRev));
		return log.execute();
	}
	
	/**
	 * Gets all the branches present for the repository 
	 * @return
	 * 		{@link List} of {@link Branch} objects representing mercurial branches.
	 */
	public List<String> getBranches() {
		
		BranchesCommand branchCommand = BranchesCommand.on(repository);
		List<Branch> branchList = branchCommand.execute();
		List<String> branches = new ArrayList<String>();
		for (Branch branch : branchList) {
			branches.add(branch.getName());
		}
		
		return branches;
	}
	
	/**
	 * Get the current changeset of the local workspace.
	 * @return
	 * 		{@link String} containing ChangeSet of the current workspace.
	 */
	public String getCurrentChangeSet() {
		
		Changeset currentChangeSet = repository.workingCopy().getParent1();
		return currentChangeSet.getNode();
	}
	
	/**
	 * Add a Tag on the current changeset with the specified tagName.
	 * @param tagNames
	 * 		{@link String} containing the tag name to be added.
	 */
	public void tag(String user, String tagName) {
		TagCommand tag = TagCommand.on(repository);
		tag.user(user);
		tag.execute(tagName);
	}
	
	/**
	 * Get list of all tag names in the current repository.
	 * @return
	 * 		{@link List} of {@link String} tag names in the current repository
	 */
	public List<String> getAllTags() {
		TagsCommand tags = TagsCommand.on(repository);
		List<Tag> tagList = tags.execute();
		List<String> tagNameList = new ArrayList<String>();
		
		for (Tag tag : tagList) {
			tagNameList.add(tag.getName());
		}
		
		return tagNameList;
	}
	
	/**
	 * Get list of all tag names in the current repository.
	 * @return
	 * 		{@link List} of {@link String} tag names in the current repository
	 */
	public String getTagChangeset(String tagname) {
		TagsCommand tags = TagsCommand.on(repository);
		List<Tag> tagList = tags.execute();
		String changeset = null;
		
		if(tagname!=null && tagList!=null) {
			
			// When the user manually adds a tag with format 17.4.0 then handle the scenario
			int count = StringUtils.countMatches(tagname, ".");
			String tagnameWithZero = null;
			
			if(count == 1) {
				tagnameWithZero = tagname+".0";
			}
			
			for (Tag tag : tagList) {
				
				if(tagname.equals(tag.getName())) {
					changeset = tag.getChangeset().getNode();
					break;
				}
				else if(tagnameWithZero != null && tagnameWithZero.equals(tag.getName())){
					changeset = tag.getChangeset().getNode();
					break;
				}
			}
		}
		
		return changeset;
	}
	
	/**
	 * Pushes all the current changes to the remote mercurial repository and returns all
	 * the changes pushed.
	 * @return
	 * 		{@link List} of {@link Changeset} pushed on the remote mercurial repository.
	 * @throws IOException
	 */
	public List<String> push() throws Exception {
		PushCommand push = PushCommand.on(repository);
		List<Changeset> pushList = push.execute();
		List<String> pushChanges = new ArrayList<String>();
		for (Changeset changeset : pushList) {
			pushChanges.add(changeset.getNode());
		}
		
		return pushChanges;
	}

	/**
	 * Revert all the changes for the specified files in the current repository. 
	 * @param files
	 * 		{@link File} for which the changes would be reverted.
	 * @throws IOException
	 */
	public void revert(File... files) throws IOException {
		RevertCommand revert = RevertCommand.on(repository);
		revert.noBackup();
		revert.execute(files);
	}
	
	/**
	 * Closes the previously opened/initialized mercurial repository instance.
	 * Also deletes the temporary repository configuration file. 
	 */
	public void close() {
		
		if(repository != null) {
			repository.close();
		}
		
		if(hgrc != null) {
			hgrc.delete();
		}
	}
	
	/**
	 * Gets the file text as a string for the specified mercurial revision.
	 * Command: hg cat --rev 4095
	 * @param filename
	 * 		{@link String} specifying the name of the file to read.
	 * @param revision
	 * 		number for the specified file's mercurial revision.
	 * @return
	 * 		{@link String} text in the specified file for the mercurial revision.
	 * @throws IOException
	 */
	public String getFile(String filename, String revision) throws IOException {
		
		String fileString = null;
		
		try {
			CatCommand cat = CatCommand.on(repository);
			
			if(revision != null) {
				cat.rev(revision);
			}
			
			InputStream inputStream = cat.execute(filename);
			StringWriter writer = new StringWriter();
			IOUtils.copy(inputStream, writer, "UTF-8");
			fileString = writer.toString();
		}
		catch(RuntimeException rex) {
			log.error("Exception", rex);
		}
		return fileString;
	}
	

	public void revertAllTags() throws IOException {
		File tags = new File(".hgtags");
		
		if(tags.exists()) {
			this.revert(tags);
		}
	}
}
