//   ____  __   __        ______        __
//  / __ \/ /__/ /__ ___ /_  __/__ ____/ /
// / /_/ / / _  / -_|_-<_ / / / -_) __/ _ \
// \____/_/\_,_/\__/___(@)_/  \__/\__/_// /
//  ~~~ oldes.huhuman at gmail.com ~~~ /_/
//
// SPDX-License-Identifier: Apache-2.0

package tech.oldes.google
{
	import flash.events.EventDispatcher;
	import flash.events.Event;
	import flash.events.StatusEvent;
	import flash.external.ExtensionContext;
	import flash.utils.ByteArray;
	
	public class GooglePlayExtension extends EventDispatcher
	{
		////////////////////////////////////////////////////////
		//	CONSTANTS
		//
		
		//
		//	ID and Version numbers
		public  static const EXT_CONTEXT_ID:String = GooglePlayConst.EXTENSIONID;
		private static const EXT_ID_NUMBER:int = -1;
		
		public  static const VERSION:String = GooglePlayConst.VERSION;
		private static const VERSION_DEFAULT:String = "0";
		private static const IMPLEMENTATION_DEFAULT:String = "unknown";
		
		//
		//	Error Messages
		private static const ERROR_CREATION:String = "The GooglePlayExtension context could not be created";

		////////////////////////////////////////////////////////
		//	VARIABLES
		//
		
		//
		// Singleton variables	
		private static var _instance:GooglePlayExtension;
		private static var _extContext:ExtensionContext;
		
		////////////////////////////////////////////////////////
		//	SINGLETON INSTANCE
		//
		public static function get instance():GooglePlayExtension {
			if ( !_instance ) {
				_instance = new GooglePlayExtension( new SingletonEnforcer() );
				_instance.init();
			}
			return _instance;
		}

		public function GooglePlayExtension( enforcer:SingletonEnforcer ) {
			_extContext = ExtensionContext.createExtensionContext( EXT_CONTEXT_ID, null );
			if ( !_extContext ) throw new Error( ERROR_CREATION );
			_extContext.addEventListener( StatusEvent.STATUS, onStatusHandler );
		}

		private function onStatusHandler( event:StatusEvent ):void {
			var e:Event;

			switch(event.code) {
				case GooglePlayEvent.ON_SIGN_IN_SUCCESS:
				case GooglePlayEvent.ON_SIGN_IN_FAIL:
				case GooglePlayEvent.ON_SIGN_OUT_SUCCESS:
				case GooglePlayEvent.ON_OPEN_SNAPSHOT_READY:
				case GooglePlayEvent.ON_OPEN_SNAPSHOT_FAILED:
					e = new GooglePlayEvent(event.code, event.level);
					break;
			}
			if (e) {
				this.dispatchEvent(e);
			}
		}

		private function init():void {
			_extContext.call( "init" );
		}

		public function dispose():void {
			if (_extContext) {
				_extContext.removeEventListener( StatusEvent.STATUS, onStatusHandler );
				_extContext.dispose();
				_extContext = null;
			}
			_instance = null;
		}
		

		
		
		//----------------------------------------
		//
		// Public Methods
		//
		//----------------------------------------

		public function get version():String
		{
			return VERSION;
		}

		public function get nativeVersion():String
		{
			return _extContext.call("nativeVersion") as String;
		}

		public function systemLog(message:String):void
		{
			_extContext.call("systemLog", message);
		}
		
		public function isSignedIn():Boolean {
			return _extContext.call("isSignedIn") as Boolean;
		}
		
		public function signIn():void {
			_extContext.call("signIn");
		}

		public function silentSignIn():void {
			_extContext.call("silentSignIn");
		}
		
		public function signOut():void {
			_extContext.call("signOut");
		}
		
		public function reportAchievement(achievementId:String, percent:Number = 0):void {
			_extContext.call("reportAchievement", achievementId, percent);
		}
		
		public function showStandardAchievements():void {
			_extContext.call("showStandardAchievements");
		} 
		
		public function openSnapshot(name:String):void {
			_extContext.call("openSnapshot", name);
		} 
		
		public function writeSnapshot(name:String, data:ByteArray, time:Number):void {
			_extContext.call("writeSnapshot", name, data, time);
		} 
		
		public function readSnapshot(name:String):ByteArray {
			return _extContext.call("readSnapshot", name) as ByteArray;
		} 
		
		public function deleteSnapshot(name:String):ByteArray {
			return _extContext.call("deleteSnapshot", name) as ByteArray;
		}

		public function getMainOBBFile(version:int):String {
			return _extContext.call("getMainOBBFile", version) as String;
		}
	}
}

class SingletonEnforcer {}