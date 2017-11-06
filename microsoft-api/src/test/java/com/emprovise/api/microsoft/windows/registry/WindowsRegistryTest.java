package com.emprovise.api.microsoft.windows.registry;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;


/**
 * Simple windows registry utility.
 * 
 * @author Bartosz Firyn (sarxos)
 * @author Yunqi Ouyang (oyyq99999)
 */
public class WindowsRegistryTest {

	@Test
	public void getKeyValue() throws Exception {
		WindowsRegistry reg = WindowsRegistry.getInstance();
		String tree = "SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion";
		String value = reg.readString(HKey.HKEY_LOCAL_MACHINE, tree, "ProductName");
		assertNotNull(value);
		assertFalse(value.isEmpty());
		System.out.println("Windows Distribution = " + value);
	}

	@Test
	public void getKeyValue1() throws Exception {
		WindowsRegistry reg = WindowsRegistry.getInstance();
		String tree = "Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings";
		String value = reg.readString(HKey.HKEY_CURRENT_USER, tree, "AutoConfigURL");
		reg.writeStringValue(HKey.HKEY_CURRENT_USER, tree, "AutoConfigURL", "YELLO!!");
		assertNotNull(value);
		assertFalse(value.isEmpty());
		System.out.println("Windows Distribution = " + value);
	}

/*
	@Test
	public void listKeys() throws Exception {
		WindowsRegistry reg = WindowsRegistry.getInstance();
		String branch = "SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\PrinterPorts";
		List<String> keys = reg.readStringSubKeys(HKey.HKEY_LOCAL_MACHINE, branch);
		for (String key : keys) {
			System.out.println(key);
		}
	}
*/

	@Test
	public void listValues() throws Exception {
		WindowsRegistry reg = WindowsRegistry.getInstance();
		String branch = "Software\\Microsoft\\Windows NT\\CurrentVersion\\Devices";
		Map<String, String> values = reg.readStringValues(HKey.HKEY_CURRENT_USER, branch);
		for (Map.Entry<String, String> value : values.entrySet()) {
			System.out.println(value.getKey() + ": " + value.getValue());
		}
	}

}