package com.emprovise.api.microsoft.windows.registry;

import com.emprovise.api.microsoft.windows.registry.internal.WindowsPreferencesBuilder;

import java.util.prefs.Preferences;

import static com.emprovise.api.microsoft.windows.registry.internal.WindowsPreferencesBuilder.*;


/**
 * HKEY enumeration.
 * 
 * @author Bartosz Firyn (sarxos)
 * @author Yunqi Ouyang (oyyq99999)
 */
public enum HKey {

	/**
	 * HKEY_CLASSES_ROOT
	 */
	HKEY_CLASSES_ROOT(HKCR_VALUE, WindowsPreferencesBuilder.getHKCR()),

	/**
	 * HKEY_CURRENT_USER
	 */
	HKEY_CURRENT_USER(HKCU_VALUE, Preferences.userRoot()),

	/**
	 * HKEY_LOCAL_MACHINE
	 */
	HKEY_LOCAL_MACHINE(HKLM_VALUE, Preferences.systemRoot()),

	/**
	 * HKEY_USERS
	 */
	HKEY_USERS(HKU_VALUE, WindowsPreferencesBuilder.getHKU()),

	/**
	 * HKEY_CURRENT_CONFIG
	 */
	HKEY_CURRENT_CONFIG(HKCC_VALUE, WindowsPreferencesBuilder.getHKCC());

	private int hex = 0;

	private Preferences root = null;

	HKey(final int hex, final Preferences root) {
		this.hex = hex;
		this.root = root;
	}

	public int hex() {
		return hex;
	}

	public Preferences root() {
		return root;
	}

	public static HKey fromHex(int hex) {
		HKey[] hks = HKey.values();
		for (HKey hk : hks) {
			if (hk.hex() == hex) {
				return hk;
			}
		}
		return null;
	}
}
