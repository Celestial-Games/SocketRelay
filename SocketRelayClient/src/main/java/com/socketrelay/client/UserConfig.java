package com.socketrelay.client;

import java.util.prefs.Preferences;

public class UserConfig {
	
	private static Preferences prefs = Preferences.userNodeForPackage(UserConfig.class);
	
	private static String handle;
//	private static String session;
	
	public static String getPlayFabAccountEmail() {
		return prefs.get("PlayFabAccountEmail", null);
	}
	
	public static void setPlayFabAccountEmail(String playFabAccountName) {
		prefs.put("PlayFabAccountEmail", playFabAccountName);
	}
	
	public static String getPlayFabLoginToken() {
		return prefs.get("PlayFabLoginToken", null);

	}
	
	public static void setPlayFabLoginToken(String playFabLoginToken) {
		prefs.put("PlayFabLoginToken", playFabLoginToken);
	}

	public static String getHandle() {
		if (handle==null || handle.trim().length()==0) {
			String email=getPlayFabAccountEmail();
			return email.substring(0,email.indexOf("@"));
		}
		return handle;
	}

	public static void setHandle(String handle) {
		UserConfig.handle = handle;
	}
//
//	public static String getSession() {
//		return session;
//	}
//
//	public static void setSession(String session) {
//		UserConfig.session = session;
//	}
	
	
}
