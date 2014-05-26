package com.emprovise.api.scm;

import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.CredentialsProviderUserInfo;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.URIish;

import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class CredentialConfigSessionFactory extends JschConfigSessionFactory {
	
    private String passphrase;
    
	public CredentialConfigSessionFactory() {
		super();
	}

	public CredentialConfigSessionFactory(String passphrase) {
		super();
	}

	@Override
	protected void configure(OpenSshConfig.Host hc, Session session) {
		
		CredentialsProvider provider = new CredentialsProvider() {
			@Override
			public boolean isInteractive() {
				return false;
			}

			@Override
			public boolean supports(CredentialItem... items) {
				return true;
			}

			@Override
			public boolean get(URIish uri, CredentialItem... items)
					throws UnsupportedCredentialItem {
				for (CredentialItem item : items) {
					if (item instanceof CredentialItem.StringType) {
						((CredentialItem.StringType) item).setValue(passphrase);
					}
				}
				return true;
			}
		};
		UserInfo userInfo = new CredentialsProviderUserInfo(session, provider);
		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");
		session.setConfig(config);
		session.setUserInfo(userInfo);
	}

	public String getPassphrase() {
		return passphrase;
	}

	public void setPassphrase(String passphrase) {
		this.passphrase = passphrase;
	}
}
