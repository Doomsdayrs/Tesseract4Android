// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
	repositories {
		google()
		mavenCentral()

	}
	dependencies {
		classpath("com.android.tools.build:gradle:8.2.2")

		// NOTE: Do not place your application dependencies here; they belong
		// in the individual module build.gradle files
	}
}

allprojects {
	repositories {
		mavenLocal()
		google()
		mavenCentral()
		maven("https://jitpack.io")
	}
}

val tesseract4AndroidVersion by ext("4.7.0")

tasks.register<Delete>("clean") {
	delete(rootProject.buildDir)
}
