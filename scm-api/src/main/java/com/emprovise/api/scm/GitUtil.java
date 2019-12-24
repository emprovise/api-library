package com.emprovise.api.scm;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aragost.javahg.Changeset;
import com.aragost.javahg.commands.Branch;

/**
 * @author Pranav Patil
 * Utility to perform Git operations using GitHub Java API (org.eclipse.egit.github.core)
 */
public class GitUtil extends ScmUtil {

	/**
	 * Repository instance which is used to perform all the Git operations except cloning
	 */
	private Repository repository;
	
	private Git git;
	
	private UsernamePasswordCredentialsProvider credentialsprovider;
	private String scmUrl;
	private List<String> localTags = new ArrayList<String>();
	private static Logger log = LoggerFactory.getLogger(GitUtil.class);

	/**
	 * Creates a new GitUtil Object using the path to existing repository
	 * @param directoryPath
	 * 		The location to the Existing Repository
	 * @throws IOException 
	 */
	public GitUtil(String directoryPath) throws IOException {
		super();
		
		if(!directoryPath.endsWith(Constants.DOT_GIT_EXT)) {
			if(directoryPath.endsWith("/") || directoryPath.endsWith("\\")) {
				directoryPath = directoryPath + Constants.DOT_GIT_EXT; 
			}
			else {
				directoryPath = directoryPath + File.separator + Constants.DOT_GIT_EXT;
			}
		}
		
		repository = new FileRepository(new File(directoryPath));
		git = new Git(repository);
	}

	/**
	 * Creates a new GitUtil Object using the specified directory containing the existing repository
	 * @param directory
	 * 		{@link File} representing the Existing Repository directory.
	 * @throws IOException 
	 */
	public GitUtil(File directory) throws IOException {
		super();
		
		if(!directory.getName().equals(Constants.DOT_GIT_EXT)) {
			directory = new File(directory.getPath() + File.separator + Constants.DOT_GIT_EXT);
		}
		
		repository = new FileRepository(directory);
		git = new Git(repository);
	}

	/**
	 * Creates a new GitUtil Object using the specified directory containing the existing repository
	 * along with the provided authorization credentials. 
	 * @param directory
	 * 		{@link File} representing the Existing Repository directory.
	 * @param user
	 * 		{@link String} authorized user id.
	 * @param password
	 * 		{@link String} password for authentication.
	 * @param remoteUrl
	 * 		{@link String} URL to the remote git repository.
	 * @throws IOException 
	 */
	public GitUtil(File directory, String user, String password, String remoteUrl) throws IOException {
		super();
		
		if(!directory.getName().equals(Constants.DOT_GIT_EXT)) {
			directory = new File(directory.getPath() + File.separator + Constants.DOT_GIT_EXT);
		}
		
		this.repository = new FileRepository(directory);
		this.git = new Git(repository);
		this.credentialsprovider = new UsernamePasswordCredentialsProvider(user, password);
		this.scmUrl = remoteUrl;
		
		JschConfigSessionFactory sessionFactory = new CredentialConfigSessionFactory("");
		setSessionFactory(sessionFactory);
	}

	/**
	 * Creates a new GitUtil Object using the specified directory containing the existing repository.
	 * Also it sets up credentials using the SSH private key file and the provided passphrase.
	 * @param directory
	 * 		{@link File} representing the Existing Repository directory.
	 * @param privateKey
	 * 		{@link File} containing SSH private key.
	 * @param passphrase
	 * 		{@link String} passphrase for authentication of private key.
	 * @throws IOException
	 */
	public GitUtil(File directory, File privateKey, String passphrase) throws IOException {
		super();
		
		if(!directory.getName().equals(Constants.DOT_GIT_EXT)) {
			directory = new File(directory.getPath() + File.separator + Constants.DOT_GIT_EXT);
		}
		
		this.repository = new FileRepository(directory);
		this.git = new Git(repository);
		
		JschConfigSessionFactory sessionFactory = new SshConfigSessionFactory(privateKey, passphrase);
		setSessionFactory(sessionFactory);
	}

	public GitUtil(Repository repository, Git git) {
		super();
		this.repository = repository;
		this.git = git;
	}
	
	/**
	 * Pulls all the recent changes from the Repository. Note that if the remote repository requires
	 * authorization information, then git configuration 'gitrc' must be initialized. If gitrc
	 * is not initialized the pull from secured repository will return 'no changes' regardless. 
	 * @return Output log
	 * 		{@link String} containing number of changesets pulled from remote repository.
	 * @throws IOException
	 * @throws GitAPIException 
	 * @throws NoFilepatternException 
	 */
	public String pull() throws GitAPIException {
		
		git.reset().setMode(ResetType.HARD).call();
		PullResult call = git.pull().call();
		return call.getFetchResult().getMessages();
	}

	/**
	 * Updates the repository to the specified changeset
	 * @param startPoint
	 * 		The change set or the node to update the codebase 
	 * @param doClean 
	 * 		It enables to remove all the uncommitted and unversioned files 
	 * @return Output log
	 * 		{@link String} containing updated changeset.
	 * @throws IOException
	 * @throws GitAPIException  
	 */
	public String update(String startPoint, boolean doClean) throws GitAPIException, IOException {
		
	    // somehow uncommitted files sometimes pollute things. Just ditch them by resetting
		reset(startPoint, doClean);
        // make sure we're on master for our commit query
        Ref ref = git.checkout().setForce(doClean).setName(Constants.MASTER).setStartPoint(startPoint).call();
		
		//Ref ref = git.checkout().setForce(doClean).setName(Constants.MASTER).setStartPoint(startPoint).call();
		return ref.getObjectId().getName();
	}

	public String reset(String node, boolean doClean) throws GitAPIException {
		
		Ref ref = null;
		
		if(doClean) {
			ref = git.reset().setMode(ResetType.HARD).setRef(node).call();
		}
		else {
			ref = git.reset().setMode(ResetType.MIXED).setRef(node).call();
		}
		
		return ref.getObjectId().getName();
	}
	
	/**
	 * Clones the specified remote repository to local repository at the specified local path
	 * @param remoteUrl
	 * 		Url to the remote git repository
	 * @param repository
	 * 		Path to the local git repository
	 * @return
	 * 		Status of the operation
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws GitAPIException  
	 */
	public static GitUtil clone(String remoteUrl, String repository) throws GitAPIException  {

		Git gitClone = Git.cloneRepository().setURI(remoteUrl).
		setDirectory(new File(repository)).
		setBranch(Constants.MASTER).setBare(false).setRemote(Constants.DEFAULT_REMOTE_NAME).
		setNoCheckout(false).call();			
		return new GitUtil(gitClone.getRepository(), gitClone);
	}
	
	/**
	 * Clones the specified remote repository to local repository at the specified local path using JavaHg API
	 * @param remoteUrl
	 * 		Url to the remote git repository
	 * @param repository
	 * 		Path to the local git repository 		
	 * @param user
	 * 		User id to authorize the access to secured repository 
	 * @param password
	 * 		Password to authenticate the access to the specified user id
	 * @return
	 * 		A {@link GitUtil} object with the cloned {@link Repository}
	 * @throws IOException
	 */
	public static GitUtil clone(String remoteUrl, String repository, String user, String password) throws IOException {
		
		try {
			
			File localRepository = new File(repository);
			if(localRepository.exists()) {
				FileUtils.deleteQuietly(localRepository);
				localRepository.delete();
			}
			
			
			UsernamePasswordCredentialsProvider userCredentials = new UsernamePasswordCredentialsProvider(user, password);
			
			Git gitClone = Git.cloneRepository().setURI(remoteUrl).
			setDirectory(localRepository).
			setBranch(Constants.MASTER).setBare(false).setRemote(Constants.DEFAULT_REMOTE_NAME).
			setCredentialsProvider(userCredentials).
			setNoCheckout(false).call();
			
			return new GitUtil(gitClone.getRepository(), gitClone);
			
		} catch (Exception ex) {
			log.error("Exception", ex);
			return null;
		}
	}
	
	/**
	 * Returns all the changesets for the specified branch name, from the specified revision number over a period
	 * of months specified.
	 * @param branch
	 * 		{@link Branch} for which the changesets should fetched.
	 * @param months
	 * 		Number of months for which the changesets should be fetched.
	 * @param rev
	 * 		Git Revision number from which the changesets should be fetched.
	 * @return
	 * 		{@link List} of {@link Changeset} fetched from the repository 
	 */
	public List<String> getChangeSets(String branch, int months, String rev) {
		return null;
	}

	/**
	 * Get the the minimum changeset in the specified git branch.
	 * @param branch
	 * 		{@link Branch} for which the changesets should fetched.
	 * @return
	 * 		{@link Changeset} fetched from the repository
	 */
	public String getFirstChangeSet(String branch) {
			return null;
	}

	/**
	 * Get the parent changesets for the specified changeset.
	 * @param changeset
	 * 		{@link Changeset} for which the parent changesets should be fetched.
	 * @return
	 * 		{@link List} of {@link Changeset} fetched from the repository
	 * @throws IOException 
	 * @throws AmbiguousObjectException 
	 * @throws IncorrectObjectTypeException 
	 * @throws MissingObjectException 
	 * @throws RevisionSyntaxException 
	 */
	public List<ScmRevision> getParentChangesets(String changeset) throws RevisionSyntaxException, IOException  {
		
		List<ScmRevision> parentChangesets = new ArrayList<ScmRevision>();
		RevWalk walk = new RevWalk(repository);
		walk.markStart(walk.parseCommit(repository.resolve(Constants.HEAD)));
		
		ObjectId resolve = repository.resolve(changeset + "^{commit}");
		RevCommit commit = walk.parseCommit(resolve);
		
/*		TreeWalk treeWalk = new TreeWalk(repository);
	    treeWalk.addTree(commit.getTree());
	    treeWalk.setRecursive(true);
*/
	    RevCommit[] parents = commit.getParents();
	    RevCommit parsedCommit = null;
	    
	    for (RevCommit revCommit : parents) {
	    	parsedCommit = walk.parseCommit(revCommit.getId());
	    	parentChangesets.add(new ScmRevision(parsedCommit));
		}
		
		return parentChangesets;
	}
	
	/**
	 * Get all the change sets for the specified branch from the local repository. 
	 * @param branch
	 * 		{@link Branch} for which the changesets should fetched.
	 * @return
	 * 		{@link List} of {@link Changeset} fetched from the repository.
	 */
	public List<String> getChangeSets(String branch) {
		return null;
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
	public String getNextChangeSet(String branch, int rev) {
			return  null;
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
	public List<String> getChangeSets(String branch, int fromRev, int toRev) {
		return null;
	}
	
	/**
	 * Gets all the branches present for the repository 
	 * @return
	 * 		{@link List} of {@link Branch} objects representing git branches.
	 * @throws IOException 
	 */
	public List<String> getBranches() throws IOException {
		Map<String, Ref> refs = repository.getRefDatabase().getRefs(Constants.R_HEADS);
		List<String> branches = new ArrayList<String>();
		branches.addAll(refs.keySet());
		return branches;
	}
	
	/**
	 * Get the current changeset of the local workspace.
	 * @return
	 * 		{@link Changeset} of current workspace.
	 * @throws IOException 
	 */
	public String getCurrentChangeSet() throws IOException {
		Ref head = repository.exactRef(Constants.HEAD);
		return head.getObjectId().getName();
	}
	
	/**
	 * Add a Tag on the current changeset with the specified tagName.
	 * @param tagName
	 * 		{@link String} containing the tag name to be added.
	 * @throws GitAPIException    
	 */
	public void tag(String user, String tagName) throws GitAPIException {
		String escapedTagName = tagName.replace(' ', '_');
		git.tag().setName(escapedTagName).setAnnotated(true).call();
		localTags.add(Constants.R_TAGS + escapedTagName);
	}
	
	/**
	 * Get list of all tag names in the current repository.
	 * @return
	 * 		{@link List} of {@link String} tag names in the current repository
	 */
	public List<String> getAllTags() {
		List<String> tagNameList = new ArrayList<String>();
		Map<String, Ref> tags = repository.getTags();
		for (String key : tags.keySet()) {
			tagNameList.add(key);
		}
		return tagNameList;
	}
	
	/**
	 * Get list of all tag names in the current repository.
	 * @return
	 * 		{@link List} of {@link String} tag names in the current repository
	 */
	public String getTagChangeset(String tagname) {
		
		Map<String, Ref> tags = repository.getTags();
		String changeset = null;
		
		if(tagname!=null && tags!=null) {
			for (Entry<String, Ref> tag : tags.entrySet()) {
				
				if(tagname.equals(tag.getKey())) {
					changeset = tag.getValue().getObjectId().getName();
					break;
				}
			}
		}
		
		return changeset;
	}
	
	/**
	 * Pushes all the current changes to the remote git repository and returns all
	 * the changes pushed.
	 * @return
	 * 		{@link List} of {@link Changeset} pushed on the remote git repository.
	 * @throws IOException
	 * @throws GitAPIException  
	 */
	public List<String> push() throws IOException, GitAPIException {

		if(isSshIdentityAvailable()) {
			
			PushCommand pushCommand = git.push();
			pushCommand.setPushAll().setRemote(Constants.DEFAULT_REMOTE_NAME);
			setTagReferences(pushCommand);
	        
	        Iterable<PushResult> results = pushCommand.call();
			List<String> refList = new ArrayList<String>();
			
			for (PushResult pushResult : results) {
				refList.add(pushResult.getMessages());
			}
			return refList;			
		}
		else {
			return sshPushCommand();
		}
	}

	private boolean isSshIdentityAvailable() {
		
		File ssh = new File(repository.getFS().userHome(), ".ssh");
			
		if(new File(ssh, "identity").exists()) {
			return true;
		}
		else if(new File(ssh, "id_rsa").exists()) {
			return true;
		}
		else if(new File(ssh, "id_dsa").exists()) {
			return true;
		}
		else {
			return false;
		}
    }
	
	public List<String> push(String user, String password) throws IOException, GitAPIException {
		credentialsprovider = new UsernamePasswordCredentialsProvider(user, password);
		return sshPushCommand();
	}

	private List<String> sshPushCommand() throws IOException, GitAPIException {
		
		PushCommand pushCommand = git.push();
        pushCommand.setForce(true);
        pushCommand.setCredentialsProvider(credentialsprovider);
        pushCommand.setRemote(Constants.DEFAULT_REMOTE_NAME);
        setTagReferences(pushCommand);
        
		Iterable<PushResult> results = pushCommand.call();
		List<String> refList = new ArrayList<String>();
		
		for (PushResult pushResult : results) {
			refList.add(pushResult.getMessages());
		}
		return refList;
	}
	
	private void setTagReferences(PushCommand pushCommand) {
		
		if(!localTags.isEmpty()) {
			
			RefSpec[] refSpecArray = new RefSpec[localTags.size()];  
			for (int i = 0; i < localTags.size(); i++) {
				refSpecArray[i] = new RefSpec(localTags.get(i));
			}
			
			pushCommand.setRefSpecs(refSpecArray);	
		}
	}

	/**
	 * Revert all the changes for the specified files in the current repository. 
	 * @param files
	 * 		{@link File} for which the changes would be reverted.
	 * @throws IOException
	 * @throws GitAPIException  
	 * @throws InvalidRefNameException   
	 */
	public void revert(File... files) throws IOException, GitAPIException {
		
		git.checkout().setAllPaths(true).call();
//		for (File file : files) {
//			git.checkout().setForce(true).setName(Constants.MASTER).addPath(file.getAbsolutePath()).call();
//		}
	}
	
	/**
	 * Closes the previously opened/initialized git repository instance.
	 * Also deletes the temporary repository configuration file. 
	 */
	public void close() {
		git.close();
		repository.close();
	}
	
	/**
	 * Gets the file text as a string for the specified git revision.
	 * Command: git ...
	 * @param filename
	 * 		{@link String} specifying the name of the file to read.
	 * @param revision
	 * 		number for the specified file's git revision.
	 * @return
	 * 		{@link String} text in the specified file for the git revision.
	 * @throws IOException
	 */
	public String getFile(String filename, String revision) throws IOException {
		
		RevWalk walk = new RevWalk(repository);
		walk.markStart(walk.parseCommit(repository.resolve(Constants.HEAD)));
		
		ObjectId resolve = repository.resolve(revision + "^{commit}");
		RevCommit commit = walk.parseCommit(resolve);
		
		RevTree tree = commit.getTree();
		TreeWalk treeWalk = new TreeWalk(repository);
	    treeWalk.addTree(tree);
	    treeWalk.setRecursive(true);
	   treeWalk.setFilter(PathFilter.create(filename));
	    if (!treeWalk.next()) {
	      return null;
	    }
	    ObjectId objectId = treeWalk.getObjectId(0);
	    ObjectLoader loader = repository.open(objectId);
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    loader.copyTo(out);
		return out.toString();
	}

	public void revertAllTags() throws IOException {
		FileUtils.cleanDirectory(new File(repository.getDirectory(), Constants.R_TAGS)); 
	}

	public String getScmUrl() {
		Config storedConfig = repository.getConfig();
		Set<String> remotes = storedConfig.getSubsections("remote");

		if(remotes!=null && !remotes.isEmpty()) {
			return storedConfig.getString("remote", remotes.iterator().next(), "url");
		}

		return null;
	}
	
	private static void setSessionFactory(JschConfigSessionFactory sessionFactory) {

			log.info("Setting session factory !!! ");
			SshSessionFactory.setInstance(sessionFactory);
			// Create a trust manager that does not validate certificate chains
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(
						java.security.cert.X509Certificate[] certs,
						String authType) {
				}

				public void checkServerTrusted(
						java.security.cert.X509Certificate[] certs,
						String authType) {
				}
			} };
			
			// Install the all-trusting trust manager
			try {
				SSLContext sc = SSLContext.getInstance("SSL");
				sc.init(null, trustAllCerts, new java.security.SecureRandom());
				HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			} catch (GeneralSecurityException e) { 
				log.error("Exception", e);
			}
	}
	
	private Git createGitApi(File repository) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        builder.setGitDir(repository);
        return new Git(builder.setup().build());
    }
}
