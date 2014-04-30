package com.emprovise.api.scm;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.jgit.revwalk.RevCommit;

import com.aragost.javahg.Changeset;

public class ScmRevision {
	
	private String message;
	private String revision;
	private Date date;
	private String user;
	
	public ScmRevision() {
	}
	
	public ScmRevision(Changeset changeset) {
		this.revision = changeset.getNode();
		this.message = changeset.getMessage();
		this.date = changeset.getTimestamp().getDate();
		this.user = changeset.getUser();
	}
	
	public ScmRevision(RevCommit commit) {
		
		this.revision = commit.getId().getName();
		this.message = commit.getFullMessage();
		this.date = new Date(commit.getCommitTime());
		this.user = commit.getCommitterIdent().getName();
	}


	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getRevision() {
		return revision;
	}

	public void setRevision(String revision) {
		this.revision = revision;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
	
	public static ScmRevision map(Changeset changeset) {
		return new ScmRevision(changeset);
	}
	
	public static <T> List<ScmRevision> map(List<T> changesetList) {
		
		List<ScmRevision> scmRevisionList = new ArrayList<ScmRevision>();
		
		for (T changeset : changesetList) {
			if(changeset instanceof Changeset) {
				scmRevisionList.add(new ScmRevision((Changeset)changeset));
			}
			else if(changeset instanceof RevCommit) {
				scmRevisionList.add(new ScmRevision((RevCommit)changeset));
			}
		}
		return scmRevisionList;
	}
	
	
	public static ScmRevision map(RevCommit changeset) {
		return new ScmRevision(changeset);
	}

}
