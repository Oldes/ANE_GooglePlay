//   ____  __   __        ______        __
//  / __ \/ /__/ /__ ___ /_  __/__ ____/ /
// / /_/ / / _  / -_|_-<_ / / / -_) __/ _ \
// \____/_/\_,_/\__/___(@)_/  \__/\__/_// /
//  ~~~ oldes.huhuman at gmail.com ~~~ /_/
//
// SPDX-License-Identifier: Apache-2.0

package tech.oldes.GooglePlay;

import static tech.oldes.GooglePlay.GooglePlayExtension.extensionContext;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.SnapshotsClient;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.common.api.Result;


import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
@SuppressWarnings("deprecation")
public class GoogleApiHelper {
    static final String TAG = "ANE_Google_Helper";
    // Client used to sign in with Google APIs
    private GoogleSignInClient mGoogleSignInClient = null;
    private AchievementsClient mAchievementsClient = null;
    private SnapshotsClient    mSnapshotsClient = null;
    //private SavedGame mCurrentSave = null;
    private Activity mActivity = null;

    // The currently signed in account, used to check the account has changed outside of this activity when resuming.
    private GoogleSignInAccount mSignedInAccount = null;

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final int RC_SIGN_IN = 9001;
    private final static int RC_ACHIEVEMENT_UI = 9003;
    // Request code for saving the game to a snapshot.
    private static final int RC_SAVE_SNAPSHOT = 9004;
    private static final int RC_LOAD_SNAPSHOT = 9005;

    private static final int USER_NOT_SIGNED = 0;
    private static final int USER_SIGNING = 1;
    private static final int USER_SIGNED = 2;

    private GoogleSignInOptions mSignInOptions = null;


    @SuppressLint("VisibleForTests")
    public GoogleApiHelper(Activity activity) {
        mActivity = activity;
        mSignInOptions =
            new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                // Add the APPFOLDER scope for Snapshot support.
                .requestScopes(Drive.SCOPE_APPFOLDER) //not needed anymore?
                .build();
        Log.d(TAG, "GoogleSignInOptions scopes: "+ Arrays.toString(mSignInOptions.getScopeArray()));
        Log.i(TAG, "Resolving mGoogleSignInClient, activity: "+ activity);
        // Create the client used to sign in to Google services.
        mGoogleSignInClient = GoogleSignIn.getClient(activity, mSignInOptions);
        Log.d(TAG, "GoogleSignInClient: "+ mGoogleSignInClient);
    }

    public boolean isSignInAvailable() {
        return mGoogleSignInClient != null;
    }

    public boolean isSignedIn() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(GooglePlayExtension.appContext);
        Log.d(TAG, "isSignedIn account: "+ account);
        if (mSignInOptions != null && GoogleSignIn.hasPermissions(account, mSignInOptions.getScopeArray())) {
            mSignedInAccount = account;
            return true;
        }
        return false;
    }

    public void signIn() {
        Log.i(TAG, "signIn()");
        if (isSignedIn()) {
            // User is already connected, but as signIn was requested, report it...
            onConnected();
            return;
        }
        // If user is not signed, try to sign him in silently first...
        silentSignIn();
        Log.i(TAG, "signIn() end");
    }

    public void silentSignIn() {
        if (isSignInAvailable()) {
            mGoogleSignInClient.silentSignIn().addOnCompleteListener(
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            mSignedInAccount = task.getResult();
                            Log.d(TAG, "silentSignIn(): success -> "+ mSignedInAccount);
                            GooglePlayExtension.googleApiHelper.onConnected();
                        } else {
                            Exception e = task.getException();
                            // Exception with SIGN_IN_REQUIRED status code means, that verbose sign-in is required!
                            if (e instanceof ApiException && ((ApiException) e).getStatusCode() == CommonStatusCodes.SIGN_IN_REQUIRED) {
                                // Start a verbose SignIn activity....
                                try {
                                    Activity a = extensionContext.getActivity();
                                    a.startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
                                }
                                catch (Exception exception) {
                                    Log.e(TAG, "FAILED signIn: "+exception.toString());
                                    if(GooglePlayExtension.VERBOSE>2) exception.printStackTrace();
                                }
                            } else {
                                Log.e(TAG, "silentSignIn(): failure", e);
								assert e != null;
								extensionContext.dispatchEvent("ON_SIGN_IN_FAIL", e.getMessage());
                                GooglePlayExtension.googleApiHelper.onDisconnected();
                            }
                        }
                    }
                });
        }
    }

    public void signOut() {
        if(GooglePlayExtension.VERBOSE>2) Log.d(TAG, "signOut()");
        if (mGoogleSignInClient != null) {
            mGoogleSignInClient.signOut().addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        boolean successful = task.isSuccessful();
                        if(GooglePlayExtension.VERBOSE>0) Log.d(TAG, "signOut(): " + (successful ? "success" : "failed"));
                        onDisconnected();
                    }
                });
        }
    }
    public void reportAchievement(String id, double percent) {
        if(GooglePlayExtension.VERBOSE>0) Log.d(TAG, "reportAchievement: "+ id +" "+ percent+" client:"+mAchievementsClient);
        try {
            if (percent == 0) {// it means we have unlocked it.
                mAchievementsClient.unlock(id);
            } else if (percent > 0 && percent <= 1) {
                mAchievementsClient.setSteps(id, (int) (percent * 100));
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    public void showAchievements() {
        if (mAchievementsClient != null) {
            try {
                mAchievementsClient
                .getAchievementsIntent()
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        try {
                            extensionContext.getActivity().startActivityForResult(intent, RC_ACHIEVEMENT_UI);
                        }
                        catch(Exception e){
                            Log.e(TAG, "Failed to start achievements activity.");
                            if(GooglePlayExtension.VERBOSE>2) e.printStackTrace();
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "showAchievements() failed: "+ e);
                if(GooglePlayExtension.VERBOSE>2) e.printStackTrace();
            }

        }
    }

    @SuppressLint("VisibleForTests")
    public void onConnected() {
        if(GooglePlayExtension.VERBOSE>0) Log.d(TAG, "onConnected: "+ mSignedInAccount);
        assert mSignedInAccount != null;
        try {
            Context context = GooglePlayExtension.appContext;
            mAchievementsClient = Games.getAchievementsClient(context, mSignedInAccount);
            mSnapshotsClient = Games.getSnapshotsClient(context, mSignedInAccount);
			Activity activity = extensionContext.getActivity();
			if (activity != null) {
				View view = mActivity.findViewById(android.R.id.content);
				if (view != null)
					Games.getGamesClient(mActivity, mSignedInAccount).setViewForPopups(view);
			}
            extensionContext.dispatchEvent("ON_SIGN_IN_SUCCESS");
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    public void onDisconnected() {
        mSignedInAccount = null;
        mAchievementsClient = null;
        mSnapshotsClient = null;
        extensionContext.dispatchEvent("ON_SIGN_OUT_SUCCESS");
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if(GooglePlayExtension.VERBOSE>0) Log.d(TAG, "onActivityResult() request: " + requestCode +" result: "+ resultCode);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED) {
                // happens when user sign-out from achievements screen
                onDisconnected();
                return;
            }
            try {
                Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(intent);

                mSignedInAccount = task.getResult(ApiException.class);
                onConnected();
            } catch (Exception apiException) {
                String message = apiException.getMessage();
                if (message == null || message.isEmpty()) {
                    message = "unknown API error";
                }
                Log.e(TAG, "Failed to signIn: " + message);
                Log.e(TAG, apiException.toString());
                extensionContext.dispatchEvent("ON_SIGN_IN_FAIL", resultCode + "|"+message);
                onDisconnected();
            }
        } else if(requestCode == RC_ACHIEVEMENT_UI) {
            if (resultCode == GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED) {
                Log.i(TAG,"SignOut from Achievements menu");
                signOut();
            }
        }
    }

    // ========== SNAPSHOTS ======================================================
    //Create a HashMap
    private final Map<String, SavedGame> mSaveGamesData =  new HashMap<String, SavedGame>();

    private Task<SnapshotsClient.DataOrConflict<Snapshot>> waitForClosedAndOpen(final String snapshotName) {

        if(GooglePlayExtension.VERBOSE>1) Log.i(TAG, "waitForClosedAndOpen: " + snapshotName);

        return SnapshotCoordinator.getInstance()
                .waitForClosed(snapshotName)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        GooglePlayExtension.handleException(e, "There was a problem waiting for the file to close!");
                    }
                })
                .continueWithTask(new Continuation<Result, Task<SnapshotsClient.DataOrConflict<Snapshot>>>() {
                    @Override
                    public Task<SnapshotsClient.DataOrConflict<Snapshot>> then(@NonNull Task<Result> task) throws Exception {
                        Task<SnapshotsClient.DataOrConflict<Snapshot>>
                                openTask = SnapshotCoordinator.getInstance().open(mSnapshotsClient, snapshotName, true);
                        return openTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                extensionContext.dispatchEvent("openSnapshotFailed",
                                        snapshotName +" "+e.getMessage());
                                GooglePlayExtension.handleException(e, "openSnapshotFailed");
                            }
                        });
                    }
                });
    }


    public boolean openSnapshot(String name) {
        if (!isSignedIn()) return false;
        SavedGame save = mSaveGamesData.get(name);
        if(save == null) {
            save = new SavedGame(name, null, -3);
            mSaveGamesData.put(name, save);
        }
        if(save.isOpening) return true;

        save.isOpening = true;
        //mCurrentSave = save;

        waitForClosedAndOpen(name)
                .addOnSuccessListener(new OnSuccessListener<SnapshotsClient.DataOrConflict<Snapshot>>() {
                    @Override
                    public void onSuccess(SnapshotsClient.DataOrConflict<Snapshot> result) {
                        String name = null;
                        SnapshotCoordinator coord = SnapshotCoordinator.getInstance();
                        // if there is a conflict  - then resolve it.

                        Snapshot snapshot = processOpenDataOrConflict(RC_LOAD_SNAPSHOT, result, 0);

                        if (snapshot == null) {
                            Log.w(TAG, "Conflict was not resolved automatically, waiting for user to resolve.");
                        } else {
                            name = snapshot.getMetadata().getUniqueName();
                            if(GooglePlayExtension.VERBOSE>0) Log.d(TAG, "Snapshot open success: " + name);
                            try {
                                onSnapshotOpened(snapshot);
                                if(GooglePlayExtension.VERBOSE>1) Log.i(TAG, "Opened snapshot processed.");

                            } catch (IOException e) {
                                Log.e(TAG, "Error while reading snapshot contents: " + e.getMessage());
                                extensionContext.dispatchEvent("openSnapshotFailed",
                                        name +" "+e.getMessage());
                            }
                        }
                        if (!coord.isAlreadyClosing(name)) {
                            if(GooglePlayExtension.VERBOSE>0) Log.d(TAG, "discardAndClose.."+coord.isAlreadyClosing(name));
                            try {
								assert snapshot != null;
								coord.discardAndClose(mSnapshotsClient, snapshot)
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                GooglePlayExtension.handleException(e, "There was a problem discarding the snapshot!");
                                            }
                                        });
                            } catch (Exception e) {
                                Log.e(TAG, "FAILED: discardAndClose");
                                e.printStackTrace();
                            }
                        }
 //                        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
//                            mLoadingDialog.dismiss();
//                            mLoadingDialog = null;
//                        }
                    }
                });
        return true;
    }

    public SavedGame getSavedGame(String name) {
        SavedGame save = mSaveGamesData.get(name);
        if(GooglePlayExtension.VERBOSE>2) Log.d(TAG, "[getSavedGame]..."+ name+" "+save);
        //if(save == null) openSnapshot(name);
        return save;
    }

    public void saveSnapshot(String name, byte[] data, long time) {
        if(GooglePlayExtension.VERBOSE>2) Log.d(TAG, "[saveSnapshot] " + name +" signed? "+isSignedIn());
        try {
            if (!isSignedIn()) {
                extensionContext.dispatchEvent("saveSnapshotFailed", name + " user not connected!");
                return;
            }
            SavedGame save = mSaveGamesData.get(name);
            if (save == null) {
                save = new SavedGame(name, data, time);
                mSaveGamesData.put(name, save);
            } else {
                save.setData(data);
                save.setTime(time);
            }
            writeSnapshotData(save.getSnapshot(), save);
        } catch (Exception e) {
            Log.e(TAG, "Failed saveSnapshot: " + e);
        }
    }

    private void writeSnapshotData(Snapshot toWrite, SavedGame save) {
        if(GooglePlayExtension.VERBOSE>2) Log.d(TAG, "[writeSnapshotData]...writting.. " + save +" snapshot: "+toWrite);
        try {
            if (toWrite == null || toWrite.getSnapshotContents() == null || toWrite.getSnapshotContents().isClosed()) {
                if(GooglePlayExtension.VERBOSE>2) Log.d(TAG, "[writeSnapshotData]...needs to open snapshot first!");
                save.needsWrite = true;
                openSnapshot(save.getName());
            } else {

                toWrite.getSnapshotContents().writeBytes(save.getData());
                // Save the snapshot.
                SnapshotMetadataChange metadataChange = new SnapshotMetadataChange.Builder()
                        //.setCoverImage(getScreenShot())
                        .setDescription("Modified data at: " + Calendar.getInstance().getTime())
                        .setPlayedTimeMillis(save.getTime())
                        .build();
                if(GooglePlayExtension.VERBOSE>2) Log.d(TAG, "[writeSnapshotData]...commitAndClose...");
                SnapshotCoordinator.getInstance().commitAndClose(mSnapshotsClient, toWrite, metadataChange);
                //save.setSnapshot(null);
                save.needsWrite = false;
                if(GooglePlayExtension.VERBOSE>2) Log.d(TAG, "[writeSnapshotData]...done.");
                //openSnapshot(save.getName()); //for later use
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed writeSnapshotData: " + e);
        }
    }

    public void deleteSnapshot(String name) {
        if(!isSignedIn()) {
            extensionContext.dispatchEvent("deleteSnapshotFailed", name +" user not connected!");
            return;
        }
        SavedGame save = mSaveGamesData.get(name);
        if (save != null) {
            Snapshot snapshot = save.getSnapshot();
            if(GooglePlayExtension.VERBOSE>2) Log.d(TAG, "deleteSnapshot: "+name);
            save.clearData();
            if(snapshot == null) {
                save.needsDelete = true;
                openSnapshot(name);
            } else {
                //Log.d(TAG, "deleteSnapshot: "+save);
                SnapshotCoordinator.getInstance().delete(mSnapshotsClient, snapshot.getMetadata());
            }
        }
    }

    private void onSnapshotOpened(Snapshot snapshot) throws IOException {
        if (snapshot == null) {
            Log.e(TAG, "onSnapshotOpened() snapshot is NULL!");
            return;
        }
        String name = snapshot.getMetadata().getUniqueName();
        SavedGame save = mSaveGamesData.get(name);
		assert save != null;
		save.isOpening = false;
        save.setSnapshot(snapshot);

        if(GooglePlayExtension.VERBOSE>2) Log.d(TAG, "onSnapshotOpened() "+name+" "+save);

        if (save.needsWrite) {
            writeSnapshotData(snapshot, save);
        } else if (save.needsDelete) {
            deleteSnapshot(name);
        } else {
            save.setData(snapshot.getSnapshotContents().readFully());
            extensionContext.dispatchEvent("openSnapshotReady", name);
        }

    }

    /**
     * Conflict resolution for when Snapshots are opened.
     *
     * @param requestCode - the request currently being processed.  This is used to forward on the
     *                    information to another activity, or to send the result intent.
     * @param result      The open snapshot result to resolve on open.
     * @param retryCount  - the current iteration of the retry.  The first retry should be 0.
     * @return The opened Snapshot on success; otherwise, returns null.
     */
    Snapshot processOpenDataOrConflict(int requestCode,
                                       SnapshotsClient.DataOrConflict<Snapshot> result,
                                       int retryCount) {

        retryCount++;

        if (!result.isConflict()) {
            return result.getData();
        }

        if(GooglePlayExtension.VERBOSE>0) Log.i(TAG, "Open resulted in a conflict!");

        SnapshotsClient.SnapshotConflict conflict = result.getConflict();
		assert conflict != null;
		final Snapshot snapshot = conflict.getSnapshot();
        final Snapshot conflictSnapshot = conflict.getConflictingSnapshot();

     //   ArrayList<Snapshot> snapshotList = new ArrayList<Snapshot>(2);
     //   snapshotList.add(snapshot);
     //   snapshotList.add(conflictSnapshot);

        // Display both snapshots to the user and allow them to select the one to resolve.
     //    selectSnapshotItem(requestCode, snapshotList, conflict.getConflictId(), retryCount);

        // Since we are waiting on the user for input, there is no snapshot available; return null.
        return null;
    }
}
