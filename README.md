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

Our application has 2 build types (debug and release) and 4 build flavors (development, sandbox, 
demo, and production), for a total of 8 [build variants](https://developer.android.com/studio/build/build-variants.html#build-types). You can create any one of these build 
variants locally by selecting the "Build Variants" tab located at the bottom-left of Android Studio.

### Types

- **Debug**
  - created quickly and allows for usb debugging
  - automatically signed with a default keystore provided by Android Studio
  - contents can be easily opened and read, so *use for development only*
- **Release**
  - code is shrunk, optimized, and obfuscated; takes longer to build
  - signed with a real keystore; secure
  - requires all update apks to have the same signature - otherwise, the existing app will be uninstalled and the data will be wiped. to prevent this from happening, _please use the keystore we have in Dropbox to sign all release builds_

### Flavors

- **Development**
  - hits local server as API endpoint
  - used for development
- **Sandbox** 
  - hits sandbox server (which mimics production DB)
  - used for QA
- **Demo**
  - hits demo server (which holds fake data)
  - used only to demo to funders; should not be touched otherwise
- **Production** 
  - hits production server (which holds real patient data)

See the `build.gradle` file for more details on configuration changes between the different flavors.

We do most of our local development with the **developmentDebug** build variant, QA with the **sandboxRelease** variant, and final launch to users with the **productionRelease** variant.

### Creating signed Release types locally

There may be cases where you wish to generate a signed release build locally (e.g. to manually upload to HockeyApp for release). To do so:

1. go to Build > Generate Signed APK
2. use the keystore credentials in the `.env` file to sign the APK
3. **important**: when prompted to specify the signature version, check both "V1 (Iar Signature)" and "V2 (Full APK Signature)". APKs signed with only V2 [cannot be installed on Android versions lower than 7.0](http://stackoverflow.com/questions/42648499/difference-between-signature-versions-v1jar-signature-and-v2full-apk-signat).

## Running development apps against a local server

Apps with the development flavor are set up to hit a local server (`http://localhost:5000`) instead of a remote heroku endpoint (e.g. `https://uhp-sandbox.watsi.org`).
However, by default, emulators and devices don't know about their PC's local servers. (Going to `localhost:5000` on your emulator or device browser will attempt to access its _own_ server, which doesn't exist.)

In both cases, first start your local server (see [https://github.com/Watsi/uhp-backend](https://github.com/Watsi/uhp-backend) for more detailed instructions).

```
$ cd /your/path/to/uhp_backend
$ heroku local

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

Forward port 5000 on your device to port 5000 on your PC.

```
$ adb reverse tcp:5000 tcp:5000
```

And voila, that's it! As long as your phone is connected to your PC, it will be able to access your PC's localhost - even without internet. Note however that you'll need to rerun this command every time you disconnect and reconnect the USB.

### Accessing your local server from your emulator

Simply change the `API_HOST` config field in `build.gradle` to `10.0.2.2:portno`. This is the 
designated IP address for emulators to refer their computer's server.

```
buildConfigField "String", "API_HOST", "\"10.0.2.2:5000\""
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

Tests can also be run from the terminal using the command `./gradlew test` from the project root.

## Conventions

- http://source.android.com/source/code-style.html
- http://blog.smartlogic.io/2013-07-09-organizing-your-android-development-code-structure/