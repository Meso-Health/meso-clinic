# Watsi UHP Native Android App

[![CircleCI](https://circleci.com/gh/Watsi/uhp-android-app/tree/master.svg?style=svg&circle-token=69c6f960da7cb0bc04d5c94cbbba21c24cfafba7)](https://circleci.com/gh/Watsi/uhp-android-app/tree/master)

## Setting up your development environment

[Android Studio](https://developer.android.com/studio/index.html) is currently the officially supported IDE for Android development

After installing Android Studio it will walk you through downloading the most recent android sdk. You can then open up this cloned repo directly through the Android Studio UI.

[Gradle](https://gradle.org/) is the build tool used by Android Studio.

In order to run the application, you also need an `.env` file in your root directory for storing
private config settings. We can create a symlink of the remote `.env` file in Dropbox so that all
changes to the file will be synced across all developers.

```
$ ln -s /path/to/watsi/Dropbox/UHP/Android/.env /your/working/dir/for/.env
```

The simplest way to load these environment variables into your Android Studio is to launch the app from the Command Line after loading the variables.

```
$ cd /your/working/dir
$ source .env
$ open -a /Applications/Android\ Studio.app ~/Watsi/uhp-android-app
```

## Build types

Our application has 2 build types (debug and release) and 2 build flavors (production and sandbox), for a total of 4 build variants (sandboxDebug, sandboxRelease, productionDebug, and productionRelease). You can create any one of these 4 [build variants] (https://developer.android.com/studio/build/build-variants.html#build-types) locally by selecting the variant in the Tool Buttons bar of Android Studio.

The debug build types are fast to build and allow you to debug the app on a local phone. They are automatically signed with generic debug keystores. The release build types take longer to build (they are shrunk, optimized, obfuscated, etc.) and are signed with a real keystore.

Most of our release builds are automatically created by [Circle CI] (https://circleci.com/) and pushed to devices via [Hockey App] (https://www.hockeyapp.net/). However, if you need to create a release build locally that can be run on the devices without overwriting the existing apps, you'll need to sign the apk with the same release key. To do so,  download the release key file from Dropbox and save it to your `app` directory.

## Conventions

- http://source.android.com/source/code-style.html
- http://blog.smartlogic.io/2013-07-09-organizing-your-android-development-code-structure/

## Testing

Tests can be run directly through the Android Studio UI by right-clicking the test file and selecting the 'Run <test>' option (or run the entire test suite by right-clicking the entire test folder).

Tests can also be run from the terminal using the command `./gradlew test` from the project root.
