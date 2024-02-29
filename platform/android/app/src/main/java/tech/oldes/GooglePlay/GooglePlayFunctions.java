//   ____  __   __        ______        __
//  / __ \/ /__/ /__ ___ /_  __/__ ____/ /
// / /_/ / / _  / -_|_-<_ / / / -_) __/ _ \
// \____/_/\_,_/\__/___(@)_/  \__/\__/_// /
//  ~~~ oldes.huhuman at gmail.com ~~~ /_/
//
// SPDX-License-Identifier: Apache-2.0

package tech.oldes.GooglePlay;

import com.adobe.fre.FREByteArray;
import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREObject;
import com.adobe.fre.FREWrongThreadException;

import android.util.Log;

import java.io.File;
import java.nio.ByteBuffer;

public class GooglePlayFunctions
{
	static public class Init implements FREFunction {
		@Override
		public FREObject call(FREContext context, FREObject[] args) {
			GooglePlayExtension.appContext = context.getActivity().getApplicationContext();
			return null;
		}
	}

	static public class NativeVersion implements FREFunction {
		@Override
		public FREObject call(FREContext context, FREObject[] args) {
			FREObject result = null;
			try {
				result = FREObject.newObject( GooglePlayExtension.VERSION );
			} catch (Exception e) {
				//FREUtils.handleException( context, e );
			}
			return result;
		}
	}

	static public class SystemLog implements FREFunction {
		@Override
		public FREObject call(FREContext context, FREObject[] args) {
			FREObject result = null;
			try {
				result = FREObject.newObject( GooglePlayExtension.VERSION );
			} catch (Exception e) {
				//FREUtils.handleException( context, e );
			}
			return result;
		}
	}

	static public class SignInFunction implements FREFunction {
		@Override
		public FREObject call(FREContext context, FREObject[] args) {
			if (GooglePlayExtension.googleApiHelper != null)
				GooglePlayExtension.googleApiHelper.signIn();
			return null;
		}
	}
	static public class SilentSignInFunction implements FREFunction {
		@Override
		public FREObject call(FREContext arg0, FREObject[] arg1) {
			// This was removed from Play Games Services Sign In v2
			//if (GooglePlayExtension.googleApiHelper != null)
			//	GooglePlayExtension.googleApiHelper.silentSignIn();
			return null;
		}
	}
	static public class SignOutFunction implements FREFunction {
		@Override
		public FREObject call(FREContext arg0, FREObject[] arg1) {
			if (GooglePlayExtension.googleApiHelper != null)
				GooglePlayExtension.googleApiHelper.signOut();
			return null;
		}
	}
	static public class IsSignedInFunction implements FREFunction {
		@Override
		public FREObject call(FREContext arg0, FREObject[] arg1) {
			try {
				return FREObject.newObject(GooglePlayExtension.googleApiHelper.isSignedIn());
			} catch (FREWrongThreadException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	static public class ReportAchievementFunction implements FREFunction {
		@Override
		public FREObject call(FREContext arg0, FREObject[] arg1) {
			try
			{
				String id = arg1[0].getAsString();
				double percent = arg1[1].getAsDouble();
				GooglePlayExtension.googleApiHelper.reportAchievement(id, percent);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return null;
		}
	}
	static public class ShowAchievementsFunction implements FREFunction {
		@Override
		public FREObject call(FREContext arg0, FREObject[] arg1) {
			try
			{
				GooglePlayExtension.googleApiHelper.showAchievements();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			return null;
		}
	}

	static public class OpenSnapshot implements FREFunction {
		@Override
		public FREObject call(FREContext arg0, FREObject[] arg1) {
			try {
				String name = arg1[0].getAsString();
				Log.i("ANE_Google_Func", "OpenSnapshot(\""+name+"\")");
				if(name != null)
					GooglePlayExtension.googleApiHelper.openSnapshot(name);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	static public class ReadSnapshot implements FREFunction {
		@Override
		public FREObject call(FREContext arg0, FREObject[] arg1) {
			FREByteArray freByteArray = null;
			try {
				String name = arg1[0].getAsString();
				if(name == null) return null;

				SavedGame save = GooglePlayExtension.googleApiHelper.getSavedGame(name);
				if(GooglePlayExtension.VERBOSE>2) Log.d(GooglePlayExtension.TAG, "getSavedGame name: "+ name +" save: "+ save);

				if(save != null) {
					byte[] saveData = save.getData();

					if(saveData != null) {
						if(GooglePlayExtension.VERBOSE>2) Log.d(GooglePlayExtension.TAG, "bytes: "+saveData.length);
						freByteArray = FREByteArray.newByteArray();
						freByteArray.setProperty("length", FREObject.newObject(saveData.length));
						freByteArray.acquire();
						ByteBuffer bytes = freByteArray.getBytes();
						bytes.put(saveData, 0, saveData.length);
						freByteArray.release();
					} else {
						Log.e(GooglePlayExtension.TAG, "No data for save: "+name);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return freByteArray;
		}
	}

	static public class WriteSnapshot implements FREFunction {
		@Override
		public FREObject call(FREContext arg0, FREObject[] arg1) {
			try {
				String name = arg1[0].getAsString();
				FREByteArray data = (FREByteArray) arg1[1];
				if(name == null || data == null) return null;

				long time = (long) arg1[2].getAsDouble();

				data.acquire();
				ByteBuffer bb = data.getBytes();
				byte[] bytes = new byte[(int) data.getLength()];
				bb.get(bytes);

				if(GooglePlayExtension.VERBOSE>2) Log.d(GooglePlayExtension.TAG, "WriteSnapshot name: "+ name +" bytes: "+ bytes.length+" time: "+time);
				data.release();
				GooglePlayExtension.googleApiHelper.saveSnapshot(name, bytes, time);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	static public class DeleteSnapshot implements FREFunction {
		@Override
		public FREObject call(FREContext arg0, FREObject[] arg1) {
			try {
				String name = arg1[0].getAsString();
				if(name != null)
					GooglePlayExtension.googleApiHelper.deleteSnapshot(name);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	static public class GetObbDir implements FREFunction {
		@Override
		public FREObject call(FREContext freContext, FREObject[] args) {
			FREObject result = null;
			try {
				result = FREObject.newObject( GooglePlayExtension.appContext.getObbDir().getAbsolutePath() );
			} catch (Exception err) {
				//Logger.exception("GetObbDir", err);
			}
			return result;
		}
	}

	static public class GetMainOBBFile implements FREFunction {
		private static final String TAG = "GetMainOBBFile";
		@Override
		public FREObject call(FREContext freContext, FREObject[] args) {
			FREObject result = null;
			try {
				FREObject input = args[0];
				int packageVersion = input.getAsInt();
				//Logger.d(TAG, "version: " + packageVersion);
				String packageName = GooglePlayExtension.appContext.getPackageName();
				//Logger.d(TAG, "name: " + packageName);
				File file = new File(GooglePlayExtension.appContext.getObbDir()+"/main." + packageVersion + "." + packageName + ".obb");
				//Logger.d(TAG, "file: " + file);
				if (file.exists()) {
					result = FREObject.newObject( file.getAbsolutePath() );
				}
			} catch (Exception err) {
				//Logger.exception(TAG, err);
			}
			return result;
		}
	}
}
