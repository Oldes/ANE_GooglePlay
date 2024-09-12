;;   ____  __   __        ______        __
;;  / __ \/ /__/ /__ ___ /_  __/__ ____/ /
;; / /_/ / / _  / -_|_-<_ / / / -_) __/ _ \
;; \____/_/\_,_/\__/___(@)_/  \__/\__/_// /
;;  ~~~ oldes.huhuman at gmail.com ~~~ /_/
;;
;; SPDX-License-Identifier: Apache-2.0

Rebol [
	title:   "Build GooglePlay.ane"
	purpose: "Build Google Play Games AIR native extension"
	needs: 3.16.0 ;; https://github.com/Oldes/Rebol3/releases/tag/3.16.0
]

import airsdk ;== https://github.com/Oldes/Rebol-AIRSDK

make-dir %build/

air-task
"Compile GooglePlayExtension SWC" [
	compc [
		-swf-version     33
		-source-path     %platform/actionscript/src
		-include-classes %tech.oldes.google.GooglePlayExtension
		-output          %build/tech.oldes.GooglePlay.swc
	]
]

air-task
"Compile Android natives" [
	cd   %platform/android
	eval %gradlew [clean build]
	print as-green "Lint results:"
	print read/string %app/build/reports/lint-results-debug.txt
	cd %../..
	copy-file %platform/android/app/build/outputs/aar/app-release.aar %build/tech.oldes.GooglePlay.aar
]

if not exists? %resources/ [
	air-task
	"Download resources" [
		ane-dependencies %resources/ [
			"androidx.activity:activity:1.9.2"
			"com.google.android.play:asset-delivery:2.2.2"
			"com.google.android.gms:play-services-games:23.2.0"
			"com.google.android.gms:play-services-auth:21.2.0"
		]
	]
]

air-task
"Compile GooglePlayExtension ANE" [
	delete-file %build/tech.oldes.GooglePlay.ane
	build-ane [
		id:  @tech.oldes.GooglePlay
		initializer: @GooglePlayExtension
		platforms: [Android-ARM Android-ARM64 Android-x86 Android-x64]
		resources: %resources/
	]
]

if exists? %../HelloAir/Extensions/ [
	air-task
	"Copy ANE to the HelloAir test app folder" [
		copy-file %build/tech.oldes.GooglePlay.ane %../HelloAir/Extensions/
	]
]