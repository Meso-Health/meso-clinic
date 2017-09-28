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
$ open -a /Applications/Android\ Studio.app /your/working/dir
```

## Build variants

Our application has 2 build types (debug and release) and 5 build flavors (development, spec, sandbox,
demo, and production), for a total of 10 [build variants](https://developer.android.com/studio/build/build-variants.html#build-types). You can create any one of these build
variants locally by selecting the "Build Variants" tab located at the bottom-left of Android Studio.

### Types

- **Debug**
  - created quickly and allows for usb debugging
  - automatically signed with a default keystore provided by Android Studio
  - contents can be easily opened and read, so *use for development only*
- **Release**
  - code is shrunk, optimized, and obfuscated; takes longer to build
  - signed with a real keystore; secure
  - requires all update apks to have the same signature - otherwise, the existing app will be uninstalled and the data will be wiped

### Flavors

- **Development**
  - hits local server as API endpoint
  - used for development
- **Spec**
  - hits local server as API endpoint
  - used for running tests
  - this is the flavor used by circleci
- **Sandbox** 
  - hits sandbox server (which mimics production DB)
  - used for QA
- **Demo**
  - hits demo server (which holds fake data)
  - used only to demo to funders; should not be touched otherwise
- **Production** 
  - hits production server (which holds real patient data)

See the `build.gradle` file for more details on configuration changes between the different flavors.

We do most of our local development with the **developmentDebug** build variant, run tests with the **specDebug** build variant, QA with the **sandboxRelease** variant, and final launch to users with the **productionRelease** variant.

### Running vs Building

There are [many options](https://developer.android.com/studio/run/index.html) for building and running apps in Android Studio.

When you __run__ an app (Selecting `app` next to the play button and then clicking the play button _or_ going to Run -> Run... -> app), Android Studio will automatically do the following:
1. Generate the apk in app/builds/outputs/apk
2. Copy the apk to your phone using `adb install path_to_apk` (or something similar)
3. Open the app on your phone

When you __build__ an app, it will just do the first step. You can then do whatever you wish with the APK.

### Running or Building Release types

When running or building release variants of the app, Android Studio will ask that you provide a specific release key instead of using a random default key like it does for debug variants. This is because all release builds must be signed with the same signature for the device to recognize it as the same app - otherwise, the phone will wipe all the data on the old app before installing the new one. To ensure that all release builds are signed with the same key, please use the one in Dropbox.

- Go to Dropbox and search for the `release-key.jks` file.
- Download it to `/your/working/dir/app`.

To __run__ the release build on your phone, follow the steps for running an app above.

To __build__ the APK to your computer, do one of the following:

Option 1
1. Build > Build APK (this will automatically use the `signingConfigs` specified in the `build.gradle` file).
2. Go to `app/builds/outputs/apk`.

Option 2
1. Go to Build > Generate Signed APK (this will pop open a dialog).
2. Use the keystore credentials (keystore password, key name, and key password) in the `.env` file to fill out the dialog.
3. **Important**: when prompted to specify the signature version, check both "V1 (Iar Signature)" and "V2 (Full APK Signature)". APKs signed with only V2 [cannot be installed on Android versions lower than 7.0](http://stackoverflow.com/questions/42648499/difference-between-signature-versions-v1jar-signature-and-v2full-apk-signat).
4. Choose the flavor and build.

You now have a signed release APK that you can email, manually install on individual phones using `adb install`, or upload to HockeyApp for mass distribution. For more detailed instructions and up-to-date info on signing and publishing, see the [official docs](https://developer.android.com/studio/publish/app-signing.html#release-mode).

## Running app against a local server

Apps with the development/spec flavor are set up to hit a local server (`http://localhost:5000` for development and `http://localhost:8000` for spec) instead of a remote heroku endpoint (e.g. `https://uhp-sandbox.watsi.org`).
However, by default, emulators and devices don't know about their PC's local servers. (Going to `localhost:5000` on your emulator or device browser will attempt to access its _own_ server, which doesn't exist.)

In both cases, first start your local server (see [https://github.com/Watsi/uhp-backend](https://github.com/Watsi/uhp-backend) for more detailed instructions).

```
$ cd /your/path/to/uhp_backend
```

To run local server for development:
```
$ heroku local
```

To run local server for running tests:
```
$ rails server -e android-test -p 8000
```

```
# To view logs
$ tail -f log/development.log
```

### Accessing your local server from your device

To access localhost from your device, we'll be using
a command-line tool that comes pre-installed with Android called `adb` (Android Debug Bridge).

First, connect your device via USB.

Now go to the directory where `adb` is located.

```
$ cd /your/path/to/sdk/platform-tools
```


Check that your device is connected.

```
$ adb devices
```

Forward port number (depending on flavor) on your device to port number on your PC.

For Development:
```
$ adb reverse tcp:5000 tcp:5000
```

For Spec:
```
$ adb reverse tcp:8000 tcp:8000
```

And voila, that's it! As long as your phone is connected to your PC, it will be able to access your PC's localhost - even without internet. Note however that you'll need to rerun this command every time you disconnect and reconnect the USB.

### Accessing your local server from your emulator

Simply change the `API_HOST` config field in `build.gradle` to `http://10.0.2.2:portno`. This is the
designated IP address for emulators to refer their computer's server.

For Development:
```
buildConfigField "String", "API_HOST", "\"http://10.0.2.2:5000\""
```

For Spec:
```
buildConfigField "String", "API_HOST", "\"http://10.0.2.2:8000\""
```

## Continuous Deployment

We use Circle CI as our continous integration tool. A green run on `master` automatically creates a signed **sandboxRelease** build and pushes it to Android devices via [Hockey App](https://www.hockeyapp.net/). Similarly, a green run on the `production` branch automatically creates a signed **productionRelease** build and pushes via Hockey App.


### Deploy to production via hockeyapp

To deploy to production, first merge your changes into `master` and ensure you get a green build. Then:

1. Check what you are going to deploy at [https://github.com/Watsi/uhp-android-app/compare/production...master](https://github.com/Watsi/uhp-android-app/compare/production...master)

2. Locally, fetch the latest changes and merge `master` into `production`:
```
$ git pull --rebase
$ git checkout production
$ git reset --hard origin/production
$ git merge master
$ git push origin head
```

3. Once the [CI on the production branch](https://circleci.com/gh/Watsi/uhp-android-app/tree/production) goes green, the app will automatically be deployed to all phones running the production app.

## Testing

Tests can be run directly through the Android Studio UI by right-clicking the test file and selecting the 'Run <test>' option (or run the entire test suite by right-clicking the entire test folder).

Tests can also be run from the terminal. 

 ```
 # Make sure the current environment variables are loaded, otherwise this will fail to build.
 source .env

 # Run all unit tests against every build variant (this is unnecessary).
 ./gradlew test
 
 # Run all unit tests for a specific build variant.
 ./gradlew test<variant_name>
  
 # Run all feature tests against every build variant (this is unnecessary).
 ./gradlew connectedAndroidTest

 # Run all feature tests against development debug variant.
 ./gradlew connectedDevelopmentDebugAndroidTest

 # Run all feature tests for a specific build variant.
 ./gradlew connected<variant_name>AndroidTest
 
 # Run feature tests in a specific package for a specific build variant.
 ./gradlew connected<variant_name>AndroidTest -Pandroid.testInstrumentationRunnerArguments.package=<package_name>
 
 # Run specific feature test for a specific build variant.
 ./gradlew connected<variant_name>AndroidTest -Pandroid.testInstrumentationRunnerArguments.class=<package_name>
 ```
 
 More options [here](https://developer.android.com/studio/test/command-line.htm).
 
### To run offline feature tests locally:
```
 ./gradlew connectedSpecDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.package=org.watsi.uhp.offline
```

### To run end-to-end (online) feature tests locally:

In your local UHP Backend folder:

```
# Setup android-test db with seed data:
$ RAILS_ENV=android-test rails db:setup

# Run the android-test server on port 8000:
$ rails server -e android-test -p 8000

```

Then, back on the android side you can run your test locally either in Android Studio or in terminal, with either an emulator or a real connected device.

## Conventions

- http://source.android.com/source/code-style.html
- http://blog.smartlogic.io/2013-07-09-organizing-your-android-development-code-structure/
