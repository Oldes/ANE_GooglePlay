Rebol [title: "Clean repository"]

import airsdk

air-task "Clean repository" [
	try [delete-dir %temp/]
	cd   %platform/android
	eval %gradlew [clean]
	try [delete-dir %.gradle]
	try [delete-dir %.idea]
	cd   %../..
]
