import java.util.Properties
import javax.inject.Inject

fun readXcconfigValue(file: File, key: String): String? {
    if (!file.exists()) return null
    return file.readLines()
        .asSequence()
        .map(String::trim)
        .filter { it.isNotEmpty() && !it.startsWith("#") && it.contains('=') }
        .map { line ->
            val separatorIndex = line.indexOf('=')
            line.substring(0, separatorIndex).trim() to line.substring(separatorIndex + 1).trim()
        }
        .firstOrNull { (entryKey, _) -> entryKey == key }
        ?.second
}

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.sentry.android.gradle)
    alias(libs.plugins.androidx.baselineprofile)
}

val localProps = Properties().apply {
    val propsFile = rootProject.file("local.properties")
    if (propsFile.exists()) propsFile.inputStream().use { load(it) }
}
val releaseStoreFile = localProps.getProperty("NUVIO_RELEASE_STORE_FILE")?.takeIf { it.isNotBlank() }
val releaseStorePassword = localProps.getProperty("NUVIO_RELEASE_STORE_PASSWORD")?.takeIf { it.isNotBlank() }
val releaseKeyAlias = localProps.getProperty("NUVIO_RELEASE_KEY_ALIAS")?.takeIf { it.isNotBlank() }
val releaseKeyPassword = localProps.getProperty("NUVIO_RELEASE_KEY_PASSWORD")?.takeIf { it.isNotBlank() }
val releaseKeystore = releaseStoreFile?.let(rootProject::file)
fun envOrLocalProperty(key: String): String? =
    providers.environmentVariable(key).orNull?.trim()?.takeIf { it.isNotBlank() }
        ?: localProps.getProperty(key)?.trim()?.takeIf { it.isNotBlank() }

val legacyUpdateCompatSigning = envOrLocalProperty("NUVIO_LEGACY_UPDATE_COMPAT_SIGNING")
    ?.toBooleanStrictOrNull()
    ?: false
val legacyUpdateCompatKeystore = file("${System.getProperty("user.home")}/.android/debug.keystore")
val debugStoreFile = envOrLocalProperty("MEW_DEBUG_KEYSTORE_FILE")?.let(::file)
val debugStorePassword = envOrLocalProperty("MEW_DEBUG_KEYSTORE_PASSWORD")
val debugKeyAlias = envOrLocalProperty("MEW_DEBUG_KEY_ALIAS")
val debugKeyPassword = envOrLocalProperty("MEW_DEBUG_KEY_PASSWORD")
val sentryAuthToken = envOrLocalProperty("SENTRY_AUTH_TOKEN")
val sentryOrg = envOrLocalProperty("SENTRY_ORG")
val sentryProject = envOrLocalProperty("SENTRY_PROJECT")
val sentryMappingUploadEnabled = sentryAuthToken != null && sentryOrg != null && sentryProject != null
val appVersionConfigFile = rootProject.file("iosApp/Configuration/Version.xcconfig")
val releaseAppVersionName = providers.gradleProperty("nuvio.app.versionName").orNull
    ?: readXcconfigValue(appVersionConfigFile, "MARKETING_VERSION")
    ?: error("MARKETING_VERSION is missing from ${appVersionConfigFile.path}")
val releaseAppVersionCode = readXcconfigValue(appVersionConfigFile, "CURRENT_PROJECT_VERSION")
    ?.toIntOrNull()
    ?: error("CURRENT_PROJECT_VERSION is missing or invalid in ${appVersionConfigFile.path}")
val requestedTaskNames = gradle.startParameter.taskNames.map { it.substringAfterLast(':') }
val buildsReleaseApks = requestedTaskNames.any {
    it.startsWith("assemble", ignoreCase = true) && it.endsWith("Release", ignoreCase = true)
}
val hasReleaseSigningConfig = releaseKeystore?.exists() == true &&
    releaseStorePassword != null && releaseKeyAlias != null && releaseKeyPassword != null
val hasLegacyUpdateSigningConfig = legacyUpdateCompatSigning && legacyUpdateCompatKeystore.exists()

if (buildsReleaseApks && !hasReleaseSigningConfig && !hasLegacyUpdateSigningConfig) {
    error(
        "Release APK signing is not configured. Set NUVIO_RELEASE_STORE_FILE, " +
            "NUVIO_RELEASE_STORE_PASSWORD, NUVIO_RELEASE_KEY_ALIAS, and " +
            "NUVIO_RELEASE_KEY_PASSWORD in local.properties or the environment. " +
            "For legacy update-compatibility builds only, explicitly set " +
            "NUVIO_LEGACY_UPDATE_COMPAT_SIGNING=true."
    )
}

android {
    namespace = "com.nuvio.android"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileSdkMinor = libs.versions.android.compileSdkMinor.get().toInt()

    signingConfigs {
        create("debugMew") {
            if (debugStoreFile != null && debugStorePassword != null && debugKeyAlias != null && debugKeyPassword != null && debugStoreFile.exists()) {
                storeFile = debugStoreFile
                storePassword = debugStorePassword
                keyAlias = debugKeyAlias
                keyPassword = debugKeyPassword
            }
        }

        create("release") {
            if (releaseKeystore != null && releaseKeystore.exists() &&
                releaseStorePassword != null && releaseKeyAlias != null && releaseKeyPassword != null
            ) {
                storeFile = releaseKeystore
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            } else if (legacyUpdateCompatSigning && legacyUpdateCompatKeystore.exists()) {
                // Explicit opt-in only: Build 97 shipped with the Android debug certificate.
                storeFile = legacyUpdateCompatKeystore
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
            }
        }
    }

    defaultConfig {
        applicationId = "com.mew.mobile"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = releaseAppVersionCode
        versionName = releaseAppVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "distribution"
    productFlavors {
        create("full") {
            dimension = "distribution"
            proguardFiles("../composeApp/proguard-cloudstream-full.pro")
        }
        create("playstore") {
            dimension = "distribution"
        }
    }

    sourceSets.getByName("full") {
        manifest.srcFile("src/full/AndroidManifest.xml")
        jniLibs.directories.add("../composeApp/src/full/jniLibs")
    }

    sourceSets.getByName("androidTest").assets.directories.add("../testFixtures")

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            useLegacyPackaging = true
            pickFirsts += listOf(
                "lib/*/libc++_shared.so",
                "lib/*/libavcodec.so",
                "lib/*/libavutil.so",
                "lib/*/libswscale.so",
                "lib/*/libswresample.so"
            )
        }
    }

    androidResources {
        noCompress += "cvr"
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    splits {
        abi {
            isEnable = buildsReleaseApks
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            isUniversalApk = buildsReleaseApks
        }
    }

    buildTypes {
        getByName("debug") {
            if (debugStoreFile != null && debugStorePassword != null && debugKeyAlias != null && debugKeyPassword != null && debugStoreFile.exists()) {
                signingConfig = signingConfigs.getByName("debugMew")
            }
        }

        getByName("release") {
            val minifyRelease = providers.gradleProperty("releaseMinifyEnabled")
                .map(String::toBooleanStrict)
                .getOrElse(true)
            isMinifyEnabled = minifyRelease
            isShrinkResources = minifyRelease
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "../composeApp/proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

@CacheableTask
abstract class CargoNdkBuild @Inject constructor(private val exec: ExecOperations) : DefaultTask() {
    @get:Internal
    abstract val workspace: DirectoryProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sources: ConfigurableFileCollection

    @get:Input
    abstract val crate: Property<String>

    @get:Input
    abstract val abis: ListProperty<String>

    @get:Input
    abstract val minSdk: Property<Int>

    @get:Internal
    abstract val ndk: DirectoryProperty

    @get:Input
    abstract val ndkVersion: Property<String>

    @get:OutputDirectory
    abstract val jniLibs: DirectoryProperty

    @TaskAction
    fun build() {
        val out = jniLibs.get().asFile.apply {
            deleteRecursively()
            mkdirs()
        }
        exec.exec {
            workingDir = workspace.get().asFile
            environment("ANDROID_NDK_HOME", ndk.get().asFile.path)
            commandLine(
                listOf(cargo(), "ndk", "--platform", minSdk.get().toString()) +
                    abis.get().flatMap { listOf("-t", it) } +
                    listOf("-o", out.path, "build", "--release", "-p", crate.get()),
            )
        }
    }

    private fun cargo(): String {
        val home = System.getenv("CARGO_HOME")?.let(::File) ?: File(System.getProperty("user.home"), ".cargo")
        val name = if (System.getProperty("os.name").startsWith("Windows")) "cargo.exe" else "cargo"
        return File(home, "bin/$name").takeIf { it.exists() }?.path ?: "cargo"
    }
}

val native = rootProject.layout.projectDirectory.dir("native")
val rust = tasks.register<CargoNdkBuild>("buildRust") {
    workspace = native
    sources.from(fileTree(native) { exclude("target/**", "**/*.md") })
    crate = "aiostreams-native-android"
    abis = listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
    minSdk = libs.versions.android.minSdk.get().toInt()
    ndkVersion = androidComponents.sdkComponents.ndkDirectory.map { it.asFile.name }
    ndk = androidComponents.sdkComponents.ndkDirectory
    jniLibs = layout.buildDirectory.dir("cargo/jniLibs")
}

androidComponents {
    onVariants(selector().withBuildType("debug")) { variant ->
        val debugApplicationId = providers.gradleProperty("nuvioDebugApplicationId")
            .orElse("com.mew.mobile.debug")
        variant.applicationId.set(debugApplicationId)
    }
    onVariants { variant ->
        variant.sources.jniLibs?.addGeneratedSourceDirectory(rust, CargoNdkBuild::jniLibs)
    }
}

sentry {
    includeProguardMapping.set(true)
    autoUploadProguardMapping.set(sentryMappingUploadEnabled)
    uploadNativeSymbols.set(false)
    autoUploadNativeSymbols.set(false)
    includeNativeSources.set(false)
    includeSourceContext.set(false)
    autoUploadSourceContext.set(false)
    includeDependenciesReport.set(false)
    telemetry.set(false)
    sentryAuthToken?.let(authToken::set)
    sentryOrg?.let(org::set)
    sentryProject?.let(projectName::set)
    ignoredBuildTypes.set(setOf("debug"))
    autoInstallation {
        enabled.set(false)
    }
    tracingInstrumentation {
        enabled.set(false)
    }
}

baselineProfile {
    mergeIntoMain = true
    saveInSrc = true
    automaticGenerationDuringBuild = false
}

dependencies {
    implementation(project(":composeApp"))
    implementation(libs.androidx.profileinstaller)
    baselineProfile(project(":baselineprofile"))
    implementation(libs.androidx.appcompat)
    implementation(libs.compose.runtime)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    debugImplementation(libs.compose.uiTooling)
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test:core:1.7.0")
    androidTestImplementation(libs.androidx.testExt.junit)
    androidTestImplementation(libs.androidx.activity.compose)
    androidTestImplementation(libs.compose.foundation)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:${libs.versions.composeMultiplatform.get()}")
    debugImplementation("androidx.compose.ui:ui-test-manifest:${libs.versions.composeMultiplatform.get()}")
}
