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

## Build types

Our application has 3 build types (debug and release) and 2 build flavors (development, sandbox, and production), for a total of 6 build variants (developmentDebug, developmentRelease, sandboxDebug, sandboxRelease, productionDebug, and productionRelease). You can create any one of these 6 [build variants](https://developer.android.com/studio/build/build-variants.html#build-types) locally by selecting the "Build Variants" tab located at the bottom-left of Android Studio.

Debug-type build variants are created quickly and allow for usb debugging. They're automatically signed with a default generic keystore provided by Android Studio. We do most of our local development using the "developmentDebug" build variant.

Release-type build variants take longer to build (since the code is shrunk, optimized, obfuscated, etc.) and require a real keystore to sign. You can download the one stored in our Dropbox to your local `app` directory to use it. The "sandboxRelease" variant is what we use to QA, and the "productionRelease" variant is what we push to users.


Most of our release builds are automatically created by [Circle CI](https://circleci.com/) and pushed to the Android devices via [Hockey App](https://www.hockeyapp.net/).

## Running development apps against a local server

Apps with the development flavor are set up to hit a local server (`http://localhost:5000`) instead of a remote heroku endpoint (e.g. `https://uhp-sandbox.watsi.org`).
However, by default, emulators and devices don't know about their PC's local servers. (Going to `localhost:5000` on your emulator or device browser will attempt to access its _own_ server, which doesn't exist.)

In both cases, first start your local server (see [https://github.com/Watsi/uhp-backend](https://github.com/Watsi/uhp-backend) for more detailed instructions).

```
$ cd /your/path/to/uhp_backend
$ heroku local
```


### Accessing local server from  your device

To access localhost from your device, we'll be using
a command-line tool that comes pre-installed with Android called `adb` (Android Debug Bridge).

Connect your device via USB.

Go to the directory where `adb` is located.

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

### Accessing local server from your emulator

Simply change the API config field in `build.gradle` to `10.0.2.2:portno`.

```
buildConfigField "String", "API_HOST", "\"10.0.2.2:5000\""
```

## Conventions

- http://source.android.com/source/code-style.html
- http://blog.smartlogic.io/2013-07-09-organizing-your-android-development-code-structure/

## Testing

Tests can be run directly through the Android Studio UI by right-clicking the test file and selecting the 'Run <test>' option (or run the entire test suite by right-clicking the entire test folder).

Tests can also be run from the terminal using the command `./gradlew test` from the project root.

## Deploy to production via hockeyapp

We automatically deploy to our hockeyapp production app from the `production` branch. To deploy to production, first merge your changes into `master` and ensure you get a green build. Then:

1. Check what you are going to deploy at https://github.com/Watsi/uhp-android-app/compare/production...master

2. Locally, fetch the latest changes and merge `master` into `production`:
```
$ git pull --rebase
$ git co production
$ git reset --hard origin/production
$ git merge master
$ git push origin head
```

3. Once the [CI on the production branch](https://circleci.com/gh/Watsi/uhp-android-app/tree/production) goes green, the app will automatically be deployed to all phones running the production app.
