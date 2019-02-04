import com.laser.gradle.core.BasePlugin
import org.gradle.api.Project

/**
 * Plugin used to define the base plugin that will be applied to a module of the Krake library.
 * This module adds the base Android configurations defined in the gradle.properties file name "android-config".
 * If the file is not found at module level, the file will be searched in the root project.
 */
class KrakeLibPlugin extends BasePlugin {
    private Closure config

    @Override
    void execute(Project project) {
        project.with {
            def androidProps = loadProps("android-config")
            def gradleProps = loadProps("gradle")

            applyPlugin('com.android.library')
            applyPlugin('kotlin-android')
            applyPlugin('kotlin-kapt')
            applyPlugin('krake')

            // Imports the necessary modules for the krake plugin.
            krake.modules "autoOrientation"

            // Adds the Android extension.
            android {
                compileSdkVersion prop(androidProps, "COMPILE_SDK").toInteger()
                buildToolsVersion prop(androidProps, "BUILD_TOOLS")

                defaultConfig {
                    minSdkVersion prop(androidProps, "MIN_SDK").toInteger()
                    targetSdkVersion prop(androidProps, "TARGET_SDK").toInteger()
                    vectorDrawables.useSupportLibrary = true
                    consumerProguardFiles '../proguard-lib-external-rules.txt'
                    // Enable multiDex in library projects by default (used only for tests).
                    multiDexEnabled true
                    // Add the default test runner.
                    testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"
                }

                lintOptions.abortOnError = false
                sourceSets {
                    androidTest.java.srcDirs += 'src/androidTest/kotlin'
                    main.java.srcDirs += 'src/main/kotlin'
                    test.java.srcDirs += 'src/test/kotlin'
                }

                compileOptions {
                    sourceCompatibility 1.8
                    targetCompatibility 1.8
                }

                // The animations are disabled in test to avoid delay.
                testOptions.animationsDisabled true

                packagingOptions.exclude 'protobuf.meta'

                if (config != null) {
                    // Use the Android extension as delegate.
                    config.delegate = android
                    // Add the additional behavior.
                    config.call()
                }
            }

            if (!prop(gradleProps, "INTERNAL_RELEASE_MODE").toBoolean()) {
                android.buildTypes.release {
                    minifyEnabled true
                    proguardFiles getDefaultProguardFile('proguard-android.txt'), '../proguard-lib-rules.txt'
                }
            }
        }
    }
}