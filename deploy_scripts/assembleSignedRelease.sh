# Assembles a signed release APK.

export VERSION_CODE=${CIRCLE_BUILD_NUM}
export VERSION_NAME="${CURRENT_BUILD_MAJOR}.${CURRENT_BUILD_MINOR}.${CIRCLE_BUILD_NUM}"

echo "These are the versionCode and versionName that we are using to build the apk."
echo ${VERSION_CODE}
echo ${VERSION_NAME}

if [ $1 == 'production' ]; then
  ./gradlew assembleProductionRelease
elif [ $1 == 'training' ]; then
  ./gradlew assembleTrainingRelease
elif [ $1 == 'sandbox' ]; then
  ./gradlew assembleSandboxRelease
elif [ $1 == 'demo' ]; then
  ./gradlew assembleDemoRelease
else
  echo "Invalid argument for assembleSignedRelease.sh: $1"
fi
