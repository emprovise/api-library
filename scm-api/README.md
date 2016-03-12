### Source Control Management API
---

The SCM API allows to clone, load and manage the source control repository. It currently supports [Mercurial] (https://www.mercurial-scm.org/wiki/Repository) and [GIT] (https://git-scm.com/book/en/v2/Git-Basics-Getting-a-Git-Repository) repositories. The `ScmUtil` is the wrapper which provides standard common methods for checking changesets, tagging, pulling, pushing etc regardless of whether the `ScmType` is GIT or HG (Mercurial). It also provides common set of static methods to cloning or loading the repository. The SCM API uses [javahg] (https://bitbucket.org/aragost/javahg) library under the hood to interact with Mercurial and [jgit] (https://eclipse.org/jgit/) library to interact with Git. Below is the maven dependency required to use the API.

```xml
<dependency>
    <groupId>com.emprovise.api</groupId>
    <artifactId>scm-api</artifactId>
    <version>1.0</version>
</dependency>
```


Below are the few examples for using the API. 

The `cloneScmRepository` static method of `ScmUtil` takes in the Remote Git Url and user credentials to clone the remote repository on current path. The returned `ScmUtil` instance can be used to perform various operations on the cloned repository.

```java
ScmUtil scmUtil = ScmUtil.cloneScmRepository(ScmType.GIT, repoRemoteUrl, projectName, username, password);
```


The various static methods below of `ScmUtil` can be used to determine the type of repository (Git or Hg), the remote repository Url and the current branch of the repository without ever loading the repository.

```java
 File baseDirectory = new File("PATH_TO_GIT_REPO");
 
 // Find Repository Type
 ScmType scmType = ScmUtil.getScmType(baseDirectory);
 
 // Find Remote Repository URL
 String scmBaseUrl = ScmUtil.getScmUrl(baseDirectory, scmType);
 
 // Find current branch of the repository
 String branch = ScmUtil.getCurrentBranchName(baseDirectory, scmType);
```


The `loadScmRepository` static method of `ScmUtil` loads the existing repository returning an `ScmUtil` instance. While loading the existing repository, the API detects the type of repository without specifying the `ScmType`. The various instance methods allow to perform various SCM operations on the loaded repository. Since the underlying API's for mercurial and git differ the same method operations vary. The `ScmUtil` has a `GitUtil`  and `HgUtil` implementations for Git and Mercurial respectively which are invoked depending on the type of `ScmType`. When working with only a single type of repository direct SCM implementation can be used to perform custom SCM operations.
 
 ```java
 File repoDirectory = new File("PATH_TO_GIT_REPO");
 
 // Load existing repository by file path
 ScmUtil scmUtil = ScmUtil.loadScmRepository(repoDirectory, username, password);
 
 // Find last changeset or commit Id
 String changeset = scmUtil.getCurrentChangeSet();
 
 // Get the remote changes from the remote repository
 scmUtil.pull();
 
 // Tag current changes
 scmUtil.tag("USER_NAME", "TAG_DESCRIPTION");
 
 // Read all the tags in the local repository
 List<String> allStringTags = scmUtil.getAllTags();
 
 // Push the local changes to remote repository
 scmUtil.push();
 
 // Update local changes with recently pulled changes from remote Repository
 scmUtil.update(changeset, true);
 
 // Revert local changes of one ore more files with remote changes
 scmUtil.revert(new File("filename"));
 
 // Close the repository instance.
 scmUtil.close();
 ```
 