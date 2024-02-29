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
secure [%\c\Dev\Builder\ allow]

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
	cd %platform/android
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
		;resources: %services_games-23.1.0_auth-21.0.0/*
		;resources: %services-games-21.0.0_auth-20.0.0_asset-2.1.0/* ;KO
		;resources: %services-games-20.0.1_auth-19.0.0_asset-2.2.0/* ;OK
		;resources: %services-games-20.0.0_auth-19.0.0_asset-2.1.0 ;OK
		resources: %services-games-20.0.1_auth-20.0.0_asset-2.2.0
		;resources: %services-games-20.0.1_auth-20.0.0
	]
]
air-task
"Copy ANE to test app folder" [
	copy-file %build/tech.oldes.GooglePlay.ane %\c\Dev\Builder\tree\air\HelloAir\Extensions\tech.oldes.GooglePlay.ane
]