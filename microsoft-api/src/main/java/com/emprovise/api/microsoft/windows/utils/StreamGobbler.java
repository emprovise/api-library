package com.emprovise.api.microsoft.windows.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * adapted from http://www.javaworld.com/javaworld/jw-12-2000/jw-1229-traps.html?page=4
 */
public class StreamGobbler extends Thread {

	private InputStream is;
	private StringBuffer stringBuffer;
	private String lineSeparator;

	StreamGobbler(InputStream is) {
		this.is = is;
		this.stringBuffer = new StringBuffer();
		this.lineSeparator = System.getProperty("line.separator");
	}

	public void run() {

		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = reader.readLine()) != null) {
				stringBuffer.append(line);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if(reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public String getLineSeparator() {
		return lineSeparator;
	}

	public void setLineSeparator(String lineSeparator) {
		this.lineSeparator = lineSeparator;
	}

	public String getString() {
		return stringBuffer.toString();
	}
}
