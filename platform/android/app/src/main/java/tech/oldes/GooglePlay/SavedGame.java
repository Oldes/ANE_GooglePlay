//   ____  __   __        ______        __
//  / __ \/ /__/ /__ ___ /_  __/__ ____/ /
// / /_/ / / _  / -_|_-<_ / / / -_) __/ _ \
// \____/_/\_,_/\__/___(@)_/  \__/\__/_// /
//  ~~~ oldes.huhuman at gmail.com ~~~ /_/
//
// SPDX-License-Identifier: Apache-2.0

package tech.oldes.GooglePlay;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.games.snapshot.Snapshot;

import java.util.Arrays;

/**
 * Created by Oldes on 9/26/2016.
 */

public class SavedGame {
	private static final String TAG = "ANE_Google_SavedGame";
	private final String mName;
	private byte[] mData;
	private long mTime;

	private Snapshot mSnapshot;

	public SavedGame(String name, byte[] data, long time) {
		mName = name;
		mData = data;
		mTime = time;
	}
	public String getName() {
		return mName;
	}
	public byte[] getData() {
		if(GooglePlayExtension.VERBOSE>2) Log.d(TAG, mName+ " getData: "+ Arrays.toString(mData));
		return mData;
	}
	public long getTime() {
		return mTime;
	}
	public void setData(byte[] data) {
		if(GooglePlayExtension.VERBOSE>2) Log.d(TAG, mName+ " setData: "+ Arrays.toString(data));
		mData = data;
	}
	public void clearData() {
		mData = null;
		mTime = -3;
	}
	public void setTime(long time) {
		mTime = time;
	}
	public Snapshot getSnapshot() {
		return mSnapshot;
	}
	public void setSnapshot(Snapshot snapshot) {
		mSnapshot = snapshot;
		if(snapshot != null) {
			try {
				mTime = (long) snapshot.getMetadata().getPlayedTime();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public boolean needsWrite;
	public boolean needsDelete;
	public boolean isOpening;

	@NonNull
	public String toString() {
		return "[SaveGame] "
				+ mName
				+ " data: " + (mData == null ? " null" : mData.length)
				+ " time: " + mTime
				+ " opening: " + isOpening
				+ " needsWrite: " + needsWrite
				+ " mSnapshot: "+ mSnapshot;
	}
	public void dispose() {
		mData = null;
		mTime = -3;
		mSnapshot = null;
		isOpening = false;
		needsWrite = false;
		needsDelete = false;
	}
}