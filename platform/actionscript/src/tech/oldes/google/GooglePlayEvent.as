//   ____  __   __        ______        __
//  / __ \/ /__/ /__ ___ /_  __/__ ____/ /
// / /_/ / / _  / -_|_-<_ / / / -_) __/ _ \
// \____/_/\_,_/\__/___(@)_/  \__/\__/_// /
//  ~~~ oldes.huhuman at gmail.com ~~~ /_/
//
// SPDX-License-Identifier: Apache-2.0

package tech.oldes.google
{
	import flash.events.Event;
	
	public class GooglePlayEvent extends Event
	{
		public static const ON_SIGN_IN_SUCCESS      :String = "ON_SIGN_IN_SUCCESS";
		public static const ON_SIGN_IN_FAIL         :String = "ON_SIGN_IN_FAIL";
		public static const ON_SIGN_OUT_SUCCESS     :String = "ON_SIGN_OUT_SUCCESS";
		public static const ON_PLAYER_IMAGE_READY   :String = "ON_PLAYER_IMAGE_READY";
		public static const ON_SCORE_LOADED         :String = "ON_SCORE_LOADED";
		public static const ON_OPEN_SNAPSHOT_READY  :String = "openSnapshotReady";
		public static const ON_OPEN_SNAPSHOT_FAILED :String = "openSnapshotFailed";
		
		public var value:String;
		
		public function GooglePlayEvent(type:String, value:String="", bubbles:Boolean=false, cancelable:Boolean=false)
		{
			super(type, bubbles, cancelable);
			this.value = value;
		}
	}
}