# forsuredbplugin
A gradle plugin for the forsuredbproject that makes it easier to integrate with the forsuredbcompiler via your build.gradle script.

## Why should you use forsuredbplugin?
The following are the advantages of using the plugin that you don't get otherwise:

* Integration of forsuredb with your normal build process
* Automatic copying of generated assets (migration files) to your application's assets directory after successful compilation.
* Automatic generation of the correct Java Service Provider Interface declaration

If you don't use forsuredbplugin, then you have to:
* Know the correct key-value pairs for annotation processor arguments and how to pass them to your annotation processor
* Know how to set up the Java Service Provider Interface declarations for your `DBMSIntegrator` and `FSSerializerFactory` classes
* Know how migration files are generated and when/how/where to copy them in order to store them in source control and bundle them in the app

## What kind of projects does forsuredbplugin work with?

| Project Type | Works? | Note |
| ------------ | ------ | ---- |
| Java | Yes | You need some configuration that will run annotation processing for you |
| Android (Java only) | Yes | You should use the `annotationProcessor` configuration for the forsuredbcompiler dependency |
| Kotlin | Yes | You should use the `kapt` configuration provided by the `kotlin-kapt` plugin for the forsuredbcompiler dependency |
| Android (Kotlin) | Yes | You should use the `kapt` configuration provided by the `kotlin-kapt` plugin for the forsuredbcompiler dependency |
| Groovy | No | It's probably not difficult to adapt the existing code to make this work. Feel free to give it a shot and put up a PR if you're interested. |

## How do I configure the forsuredbplugin?
Add this to your build.gradle file:
```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.fsryan.gradle.forsuredb:forsuredbplugin:0.6.3'
    }
}
```

Pay attention to the order in which the plugins are applied. forsuredb needs to be applied before kotlin-android if you're using kotlin in an android project. If the order is wrong, then the annotation processor will fail.
```groovy
apply plugin: 'com.android.application' // or 'com.android.library' or 'java' or 'kotlin'
apply plugin: 'com.fsryan.gradle.forsuredb'
apply plugin: 'kotlin-android'// if kotlin-android
apply plugin: 'kotlin-kapt' // if kotlin or kotlin-android

dependencies {
    annotationProcessor 'com.fsryan.forsuredb:forsuredbcompiler:version'    // if Android but not kotlin-android
    apt 'com.fsryan.forsuredb:forsuredbcompiler:version'                    // if java (where apt is your annotation processor configuration)
    kapt 'com.fsryan.forsuredb:forsuredbcompiler:version'                   // if kotlin or kotlin-android
}

forsuredb {
    applicationPackageName = 'com.forsuredb.testapp'            // The base package for your app
    resultParameter = "android.net.Uri"                         // The class you would like to use as the result of saving records
    recordContainer = "com.forsuredb.provider.FSContentValues"  // The class you would like to put record information into before saving
    migrationDirectory = 'app/src/main/assets'                  // The assests directory for your app relative to the working directory of your build
    appProjectDirectory = 'app'                                 // The base directory for your app relative to the working directory of your build
    resourcesDirectory = 'app/src/main/resources'               // The directory that will contain META-INF/services so that your plugins can get picked up at runtime
    fsSerializerFactoryClass = 'my.json.adapter.FactoryClass'   // (optional) A class implementing FSSerializerFactory used to create your own custom serializer for object document storage
    dbmsIntegratorClass = 'com.fsryan.forsuredb.sqlitelib.SqlGenerator' // NOT OPTIONAL as of fosuredbplugin 0.4.0
}
```

## Revisions

### 0.6.3
- Fixes issue with Android and Fabric by not attempting to add migration assets to assembled binary in the same build as a `dbMigrate` is being run (for an Android project). The alternative is to run `dbMigrate` and any task that merges assets as separate builds, with the `dbMigrate` build run first so that the output assets are picked up by the second build.
- Fixes issue in a Java/Kotlin project (non-android) where migration asset copying was failing.

### 0.6.2
- fixes issues in java and kotlin projects where SPI declarations and migrations would not get added to the processed resources. Now you can run in one command: `./gradlew dbMigrate run`, and regardless of whether your migrations or SPI declarations existed prior to this command, the tasks will be run in the correct order to be included in the run.

### 0.6.1
- fixes a bug wherein the SPI declarations would not get added to the resulting Android apk if they did not exist prior to assembly

### 0.6.0
- fixes bugs integrating with java-only projects
- fixes bugs integrating with kotlin and kotlin-android projects

### 0.5.0
- Only valid for use with forsuredbcompiler 0.13.0
- reworks the plugin to be compatible with android and with java projects

### 0.4.0
- Added task to properly configure the DBMSIntegrator plugin whenever app is assembled

### 0.3.2
- Changed task that configured the FSJsonAdapterFactory plugin to configure the FSSerializerFactory plugin

### 0.3.1
- Added task to properly configure the FSJsonAdapterFactory plugin whenever app is assembled

### 0.3.0
- Support setting fsJsonAdapterFactoryClass system property.

### 0.2.1
- Some bug fixes and first reliable version
