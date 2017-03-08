# Create some env vars
export VERSION_CODE=${CIRCLE_BUILD_NUM}
export VERSION_NAME="${CURRENT_BUILD_MAJOR}.${CURRENT_BUILD_MINOR}.${CIRCLE_BUILD_NUM}"

echo "These are the verisonCode and versionName that we are using to build the apk."
echo ${VERSION_CODE}
echo ${VERSION_NAME}

if [ $1 == 'sandbox' ]; then
  # Create a release candidate
  ./gradlew assembleSandboxRelease
else
  ./gradlew assembleProductionRelease
fi
