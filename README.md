# Meso Clinic Android App

[![CircleCI](https://circleci.com/gh/Watsi/meso-clinic/tree/master.svg?style=svg&circle-token=69c6f960da7cb0bc04d5c94cbbba21c24cfafba7)](https://circleci.com/gh/Watsi/meso-clinic/tree/master)

## Setting up your development environment

[Android Studio](https://developer.android.com/studio/index.html) is currently the officially supported IDE for Android development

After installing Android Studio it will walk you through downloading the most recent android sdk. You can then open up this cloned repo directly through the Android Studio UI.

[Gradle](https://gradle.org/) is the build tool used by Android Studio.

In order to run the application, you also need a `variables.gradle` file in your root directory which stores environment variables. Create a file called `variables.gradle` and copy the file contents from 1Password.

## Build variants

Our app has several different [build variants](https://developer.android.com/studio/build/build-variants.html#build-types) to represent different environments. You can create any one of these build
variants locally by selecting the "Build Variants" tab located at the bottom-left of Android Studio.

### Default build types

- **Debug**
  - created quickly and allows for usb debugging
  - automatically signed with a default keystore provided by Android Studio
  - contents can be easily opened and read, so *use for development only*
- **Release**
  - code is shrunk, optimized, and obfuscated; takes longer to build
  - signed with a real keystore; secure
  - requires all update apks to have the same signature - otherwise, the existing app will be uninstalled and the data will be wiped

### Summary

See `build.gradle` for full details on configuration differences between the different variants.

### Running vs Building

There are [many options](https://developer.android.com/studio/run/index.html) for building and running apps in Android Studio.

When you __run__ an app (Selecting `app` next to the play button and then clicking the play button _or_ going to Run -> Run... -> app), Android Studio will automatically do the following:
1. Generate the apk in app/builds/outputs/apk
2. Copy the apk to your phone using `adb install path_to_apk` (or something similar)
3. Open the app on your phone

When you __build__ an app, it will just do the first step. You can then do whatever you wish with the APK.

### Running or Building Release types

When running or building release variants of the app, Android Studio will ask that you provide a specific release key instead of using a random default key like it does for debug variants. This is because all release builds must be signed with the same signature for the device to recognize it as the same app - otherwise, the phone will wipe all the data on the old app before installing the new one. To ensure that all release builds are signed with the same key, please use the one in 1Password.

- Go to 1Password and search for the `release-key.jks` file.
- Download it to `/your/working/dir/app`.

To __run__ the release build on your phone, follow the steps for running an app above.

To __build__ the APK to your computer, do one of the following:

Option 1
1. Build > Build APK (this will automatically use the `signingConfigs` specified in the `build.gradle` file).
2. Go to `app/builds/outputs/apk`.

Option 2
1. Go to Build > Generate Signed APK (this will pop open a dialog).
2. Use the keystore credentials (keystore password, key name, and key password) in the `variables.gradle` file to fill out the dialog.
3. **Important**: when prompted to specify the signature version, check both "V1 (Iar Signature)" and "V2 (Full APK Signature)". APKs signed with only V2 [cannot be installed on Android versions lower than 7.0](http://stackoverflow.com/questions/42648499/difference-between-signature-versions-v1jar-signature-and-v2full-apk-signat).
4. Choose the flavor and build.

You now have a signed release APK that you can email, manually install on individual phones using `adb install`, or upload to Google Play for mass distribution. For more detailed instructions and up-to-date info on signing and publishing, see the [official docs](https://developer.android.com/studio/publish/app-signing.html#release-mode).

## Running app against a local server

Apps with the development/spec flavor are set up to hit a local server (`http://localhost:5000` for development) instead of a remote heroku endpoint (e.g. `https://uhp-sandbox.watsi.org`).
However, by default, emulators and devices don't know about their PC's local servers. (Going to `localhost:5000` on your emulator or device browser will attempt to access its _own_ server, which doesn't exist.)

In both cases, first start your local server (see [https://github.com/meso-health/meso-backend](https://github.com/meso-health/meso-backend) for more detailed instructions).

```
$ cd /your/path/to/uhp_backend
```

To run local server for development (on the backend repo):
```
$ rails s -p 5000
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

And voila, that's it! As long as your phone is connected to your PC, it will be able to access your PC's localhost - even without internet. Note however that you'll need to rerun this command every time you disconnect and reconnect the USB.

### Accessing your local server from your emulator

Simply change the `API_HOST` config field in `build.gradle` to `http://10.0.2.2:portno`. This is the
designated IP address for emulators to refer their computer's server.

For Development:
```
buildConfigField "String", "API_HOST", "\"http://10.0.2.2:5000\""
```

## Continuous Deployment

Whenever code is merged into a branch with continuous deployment setup (see summary table above), circle CI automatically runs the following if tests complete:

1. Keystore, gradle variables, and google play key are downloaded from S3.
    - This requires the following [env variables](https://circleci.com/gh/Watsi/uhp-android-app/edit#env-vars) to be set in Circle:
      - GOOGLE_PLAY_KEY_S3_URI
      - GRADLE_VARIABLES_S3_URI
      - ANDROID_SIGNING_KEY_S3_URI
2. APK is built and signed with the keystore.
    - The build variant is determined by the github branch (see summary table above)
    - The app's VERSION_CODE is determined by the CIRCLE_BUILD_NUM
    - The app's VERSION_NAME is `versionMajor, versionMinor, versionPatch (CIRCLE_BUILD_NUM)`.
3. APK is deployed to Google Play.

(See `.circleci/config.yml` for source code)


## Testing

Tests can be run directly through the Android Studio UI by right-clicking the test file and selecting the 'Run <test>' option (or run the entire test suite by right-clicking the entire test folder).

Because some of our tests are run using [Roboelectric](http://robolectric.org/), we must also edit the default working directory for our JUnit environment to `$MODULE_DIR$` as described [here](http://robolectric.org/getting-started/#note-for-linux-and-mac-users).

Tests can also be run from the terminal.

 ```
 # Run all unit tests for a specific build variant.
 ./gradlew test<variant_name>
 ```
 More options [here](https://developer.android.com/studio/test/command-line.htm).

## Conventions

- http://source.android.com/source/code-style.html
- http://blog.smartlogic.io/2013-07-09-organizing-your-android-development-code-structure/

### Code Style

In general, these should be your settings:

<img width="1000" alt="screen shot 2018-12-26 at 2 05 03 am" src="https://user-images.githubusercontent.com/4009333/50436602-be383180-08b4-11e9-8559-4f19aaa48118.png">
<img width="1000" alt="screen shot 2018-12-26 at 2 05 59 am" src="https://user-images.githubusercontent.com/4009333/50436603-bed0c800-08b4-11e9-8777-b023444e39b6.png">
<img width="1000" alt="screen shot 2018-12-26 at 2 03 10 am" src="https://user-images.githubusercontent.com/4009333/50436601-be383180-08b4-11e9-8bd4-50d0b8fbcfda.png">

#### Multiline Method Signatures
> When a function signature does not fit on a single line, break each parameter declaration onto its own line. Parameters defined in this format should use a single indent (+4). The closing parenthesis ()) and return type are placed on their own line with no additional indent. [[source 1](https://kotlinlang.org/docs/reference/coding-conventions.html#function-formatting)] [[source 2](https://android.github.io/kotlin-guides/style.html#functions)]
```kt
fun longMethodName(
    argument: ArgumentType = defaultValue,
    argument2: AnotherArgumentType
): ReturnType {
    // body
}
```

#### Multiline Method Calls
> In long argument lists, put a line break after the opening parenthesis. Indent arguments by 4 spaces. Group multiple closely related arguments on the same line. [[source](https://kotlinlang.org/docs/reference/coding-conventions.html#method-call-formatting)]
```kt
drawSquare(
    x = 10, y = 10,
    width = 100, height = 100,
    fill = true
)
```

#### Continuation Indents
> When line-wrapping, each line after the first (each continuation line) is indented at least +8 from the original line. [[source](https://android.github.io/kotlin-guides/style.html#continuation-indent)]

#### Imports
Avoid wildcard imports since it imports unnecessary files and makes it unclear to the reader which specific files are being used.
