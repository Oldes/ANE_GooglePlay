//   ____  __   __        ______        __
//  / __ \/ /__/ /__ ___ /_  __/__ ____/ /
// / /_/ / / _  / -_|_-<_ / / / -_) __/ _ \
// \____/_/\_,_/\__/___(@)_/  \__/\__/_// /
//  ~~~ oldes.huhuman at gmail.com ~~~ /_/
//
// SPDX-License-Identifier: Apache-2.0

package tech.oldes.GooglePlay;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import com.adobe.fre.FREContext;
import com.adobe.fre.FREExtension;
import com.google.android.gms.common.api.ApiException;

public class GooglePlayExtension implements FREExtension
{
	public static final String TAG = "ANE_GooglePlay";
	public static final String VERSION = "1.3.1 (Games: 23.2.0 Auth: 21.2.0 Asset: 2.2.2)";
	public static final int VERBOSE = 1;

	public static GooglePlayExtensionContext extensionContext;
	public static Context appContext;
	@SuppressLint("StaticFieldLeak")
	public static GoogleApiHelper googleApiHelper;

	@Override
	public FREContext createContext(String contextType) {
		return extensionContext = new GooglePlayExtensionContext();
	}

	@Override
	public void dispose() {
		if(VERBOSE > 0) Log.i(TAG, "Extension disposed.");
		appContext = null;
		extensionContext = null;
	}

	@Override
	public void initialize() {
		if(VERBOSE > 0) Log.i(TAG, "Extension initialized.");
	}

	public static void log(String message)
	{
		extensionContext.dispatchStatusEventAsync("LOGGING", message);
	}


	public static void handleException(Exception exception, String details) {
		int status = 0;
		Log.e(TAG, "handleException: "+exception.getMessage());
		if (exception instanceof ApiException) {
			ApiException apiException = (ApiException) exception;
			status = apiException.getStatusCode();
		}
		String message = details + " (status "+ status +"). "+ exception.getMessage();
		Log.i(TAG, message);

		/*
		Activity activity = GoogleExtensionContext.getMainActivity();
		new AlertDialog.Builder(activity)
				.setMessage(message)
				.setNeutralButton(android.R.string.ok, null)
				.show();
		 */
		// Note that showing a toast is done here for debugging. Your application should
		// resolve the error appropriately to your app.
		/*
		if (status == GamesClientStatusCodes.SNAPSHOT_NOT_FOUND) {
			Log.i(TAG, "Error: Snapshot not found");
			Toast.makeText(activity.getBaseContext(), "Error: Snapshot not found",
					Toast.LENGTH_SHORT).show();
		} else if (status == GamesClientStatusCodes.SNAPSHOT_CONTENTS_UNAVAILABLE) {
			Log.i(TAG, "Error: Snapshot contents unavailable");
			Toast.makeText(activity.getBaseContext(), "Error: Snapshot contents unavailable",
					Toast.LENGTH_SHORT).show();
		} else if (status == GamesClientStatusCodes.SNAPSHOT_FOLDER_UNAVAILABLE) {
			Log.i(TAG, "Error: Snapshot folder unavailable");
			Toast.makeText(activity.getBaseContext(), "Error: Snapshot folder unavailable.",
					Toast.LENGTH_SHORT).show();
		}
		*/
	}
}
