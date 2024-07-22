plugins {
	id("com.android.library")
	id("maven-publish")
}

android {
	namespace = "cz.adaptech.tesseract4android"
	compileSdk = 33
	ndkVersion = "25.1.8937393"

	defaultConfig {
		minSdk = 16
		lint.targetSdk = 33

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		externalNativeBuild {
			cmake {
				// Specifies which native libraries or executables to build and package.
				// TODO: Include eyes-two in some build flavor of the library?
				//targets "jpeg", "pngx", "leptonica", "tesseract"
			}
		}
		ndk {
			// Specify the ABI configurations that Gradle should build and package.
			// By default it compiles all available ABIs.
			//abiFilters "x86", "x86_64", "armeabi-v7a", "arm64-v8a"
		}
	}
	externalNativeBuild {
		cmake {
			path("src/main/cpp/CMakeLists.txt")
			version = "3.22.1"
		}
	}
	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}
		debug {
			externalNativeBuild {
				cmake {
					// Force building release version of native libraries even in debug variant.
					// This is for projects that has direct dependency on this library,
					// but doesn"t really want its debug version, which is very slow.
					// Note that this only affects native code.
					arguments("-DCMAKE_BUILD_TYPE=Release")
				}
			}
		}
	}
	flavorDimensions += listOf("parallelization")
	productFlavors {
		create("standard") {
		}
		create("openmp") {
			externalNativeBuild {
				cmake {
					// NOTE: We must add -static-openmp argument to build it statically,
					// because shared library is not being included in the resulting APK.
					// See: https://github.com/android/ndk/issues/1028
					// Use of that argument shows warnings during build:
					// > C/C++: clang: warning: argument unused during compilation: "-static-openmp" [-Wunused-command-line-argument]
					// But it has no effect on the result.
					cFlags("-fopenmp -static-openmp -Wno-unused-command-line-argument")
					cppFlags("-fopenmp -static-openmp -Wno-unused-command-line-argument")
				}
			}
		}
	}
	publishing {
		singleVariant("standardRelease") {
			withSourcesJar()
			withJavadocJar()
		}
		singleVariant("openmpRelease") {
			withSourcesJar()
			withJavadocJar()
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
	buildFeatures {
		buildConfig = true
	}
}

dependencies {
	// Intentionally use old version of annotation library which doesn"t depend on kotlin-stdlib
	// to not unnecessarily complicate client projects due to potential duplicate class build errors
	// caused by https://kotlinlang.org/docs/whatsnew18.html#updated-jvm-compilation-target
	//noinspection GradleDependency
	implementation("androidx.annotation:annotation:1.3.0")

	testImplementation("junit:junit:4.13.2")
	androidTestImplementation("androidx.test:runner:1.5.2")
	androidTestImplementation("androidx.test:rules:1.5.0")
	androidTestImplementation("androidx.test.ext:junit:1.1.5")
	androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

val tesseract4AndroidVersion: String by rootProject.extra

afterEvaluate {
	publishing {
		publications {
			create<MavenPublication>("standard") {
				from(components.findByName("standardRelease"))

				groupId = "cz.adaptech"
				artifactId = "tesseract4android"
				version = tesseract4AndroidVersion
			}
			create<MavenPublication>("openmp") {
				from(components.findByName("openmpRelease"))

				groupId = "cz.adaptech"
				artifactId = "tesseract4android-openmp"
				version = tesseract4AndroidVersion
			}
		}
	}
}
