# Watsi UHP Native Android App

[![CircleCI](https://circleci.com/gh/Watsi/uhp-android-app/tree/master.svg?style=svg&circle-token=69c6f960da7cb0bc04d5c94cbbba21c24cfafba7)](https://circleci.com/gh/Watsi/uhp-android-app/tree/master)

## Setting up your development environment

[Android Studio](https://developer.android.com/studio/index.html) is currently the officially supported IDE for Android development

After installing Android Studio it will walk you through downloading the most recent android sdk. You can then open up this cloned repo directly through the Android Studio UI.

[Gradle](https://gradle.org/) is the build tool used by Android Studio.

In order to run the application, you must also create a resource file for storing private config settings. Do this by creating an xml file in `app/src/main/res/xml/secret.xml` with the following format:

```
<?xml version="1.0" encoding="utf-8"?>
<secret>
    <entry key="ROLLBAR_API_KEY">################################</entry>
    <entry key="ROLLBAR_ENV_KEY">sandbox</entry>
    <entry key="API_HOST">https://uhp-sandbox.watsi.org/</entry>
    <entry key="PROVIDER_ID">#</entry>
    <entry key="SIMPRINTS_API_KEY">########-####-####-####-############</entry>
</secret>
```

## Conventions

- http://source.android.com/source/code-style.html
- http://blog.smartlogic.io/2013-07-09-organizing-your-android-development-code-structure/

## Testing

Tests can be run directly through the Android Studio UI by right-clicking the test file and selecting the 'Run <test>' option (or run the entire test suite by right-clicking the entire test folder).

Tests can also be run from the terminal using the command `./gradlew test` from the project root.
