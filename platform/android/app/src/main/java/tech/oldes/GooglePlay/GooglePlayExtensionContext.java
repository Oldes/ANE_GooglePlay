//   ____  __   __        ______        __
//  / __ \/ /__/ /__ ___ /_  __/__ ____/ /
// / /_/ / / _  / -_|_-<_ / / / -_) __/ _ \
// \____/_/\_,_/\__/___(@)_/  \__/\__/_// /
//  ~~~ oldes.huhuman at gmail.com ~~~ /_/
//
// SPDX-License-Identifier: Apache-2.0

package tech.oldes.GooglePlay;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;

import com.adobe.air.AndroidActivityWrapper;
import com.adobe.air.ActivityResultCallback;
import com.adobe.air.AndroidActivityWrapper.ActivityState;
import com.adobe.air.StateChangeCallback;
import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;


public class GooglePlayExtensionContext extends FREContext implements
	ActivityResultCallback,
	StateChangeCallback
{
	private AndroidActivityWrapper aaw = null;

	public GooglePlayExtensionContext()
	{
		aaw = AndroidActivityWrapper.GetAndroidActivityWrapper();
		aaw.addActivityResultListener(this);
		aaw.addActivityStateChangeListner(this);
		GooglePlayExtension.googleApiHelper = new GoogleApiHelper(getActivity());
	}

	public Activity getActivity() {
		if (aaw != null) {
			return aaw.getActivity();
		}
		return null;
	}
	public static Activity getMainActivity()
	{
		return GooglePlayExtension.extensionContext.getActivity();
	}



	@Override
	public void dispose() {
		if(GooglePlayExtension.VERBOSE > 0) Log.i(GooglePlayExtension.TAG,"Context disposed.");
	}

	@Override
	public Map<String, FREFunction> getFunctions() {
		Map<String, FREFunction> functions = new HashMap<>();
		functions.put("init", new GooglePlayFunctions.Init());
		functions.put("nativeVersion", new GooglePlayFunctions.NativeVersion());
		functions.put("systemLog", new GooglePlayFunctions.SystemLog());

		functions.put("signIn", new GooglePlayFunctions.SignInFunction());
		functions.put("silentSignIn", new GooglePlayFunctions.SilentSignInFunction());
		functions.put("signOut", new GooglePlayFunctions.SignOutFunction());
		functions.put("isSignedIn", new GooglePlayFunctions.IsSignedInFunction());
		functions.put("reportAchievement", new GooglePlayFunctions.ReportAchievementFunction());
		functions.put("showStandardAchievements", new GooglePlayFunctions.ShowAchievementsFunction());

		functions.put("openSnapshot", new GooglePlayFunctions.OpenSnapshot());
		functions.put("writeSnapshot", new GooglePlayFunctions.WriteSnapshot());
		functions.put("readSnapshot", new GooglePlayFunctions.ReadSnapshot());
		functions.put("deleteSnapshot", new GooglePlayFunctions.DeleteSnapshot());

		functions.put("getMainOBBFile", new GooglePlayFunctions.GetMainOBBFile());

		return functions;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if(GooglePlayExtension.VERBOSE>1) Log.d(GooglePlayExtension.TAG, "onActivityResult: "+ requestCode +" "+ resultCode );
		GooglePlayExtension.log("ExtensionContext.onActivityResult" +
				" requestCode:" + requestCode +
				" resultCode:" + resultCode);

		if (GooglePlayExtension.googleApiHelper != null) {
			GooglePlayExtension.googleApiHelper.onActivityResult(requestCode, resultCode, intent);
		}
	}

	@Override
	public void onActivityStateChanged( AndroidActivityWrapper.ActivityState state ) {
		switch ( state ) {
			case RESUMED:
				GooglePlayExtension.googleApiHelper.isSignedIn();
				break;
			case STARTED:
			case RESTARTED:
			case PAUSED:
			case STOPPED:
			case DESTROYED:
		}
		if(GooglePlayExtension.VERBOSE>1) Log.d(GooglePlayExtension.TAG, "onActivityStateChanged: "+ state);
	}
	@Override
	public void onConfigurationChanged(Configuration paramConfiguration) {
		if(GooglePlayExtension.VERBOSE>1) Log.d(GooglePlayExtension.TAG, "onConfigurationChanged: "+ paramConfiguration);
	}

	public void logEvent(String eventName) {
		if(GooglePlayExtension.VERBOSE>0) Log.i(GooglePlayExtension.TAG, eventName);
	}
	public void dispatchEvent(String eventName) {
		dispatchEvent(eventName, "OK");
	}
	public void dispatchEvent(String eventName, String eventData)
	{
		//logEvent(eventName);
		if (eventData == null) eventData = "OK";
		dispatchStatusEventAsync(eventName, eventData);
	}
}
