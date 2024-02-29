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

import air-tools

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

air-task
"Compile GooglePlayExtension ANE" [
	delete-file %build/tech.oldes.GooglePlay.ane
	build-ane [
		id:  @tech.oldes.GooglePlay
		initializer: @GooglePlayExtension
		platforms: [Android-ARM Android-ARM64 Android-x86 Android-x64]
		resources: %services-games-20.0.1_auth-20.0.0_asset-2.2.0
	]
]

if exists? %../HelloAir/Extensions/ [
	air-task
	"Copy ANE to the HelloAir test app folder" [
		copy-file %build/tech.oldes.GooglePlay.ane %../HelloAir/Extensions/
	]
]