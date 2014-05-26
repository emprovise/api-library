package com.emprovise.api.scm;

import java.io.File;

import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.util.FS;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SshConfigSessionFactory extends JschConfigSessionFactory {
	
    private File privateKey;
    private String passphrase;

	public SshConfigSessionFactory(File privateKey, String passphrase) {
		super();
		this.privateKey = privateKey;
		this.passphrase = passphrase;
	}

	@Override
	protected void configure(OpenSshConfig.Host hc, Session session) {
		
		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");
		config.put("PreferredAuthentications", "publickey");
		session.setConfig(config);

        try {
    		JSch jsch = super.getJSch(hc, FS.DETECTED);
			jsch.addIdentity(privateKey.getAbsolutePath(), passphrase.getBytes());
		} catch (JSchException e) {
			e.printStackTrace();
		}
	}

	public File getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(File privateKey) {
		this.privateKey = privateKey;
	}

	public String getPassphrase() {
		return passphrase;
	}

	public void setPassphrase(String passphrase) {
		this.passphrase = passphrase;
	}
}