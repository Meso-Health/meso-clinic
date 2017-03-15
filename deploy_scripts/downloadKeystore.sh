# use curl to download a keystore from $KEYSTORE_URI, if set,
# to the path/filename set in $KEYSTORE.
if [[ ${ANDROID_SIGNING_KEY_URI} && ${ANDROID_SIGNING_KEY_SAVE_PATH} ]]
then
    echo "Valid ANDROID_SIGNING_KEY_SAVE_PATH and ANDROID_SIGNING_KEY_URI  detected - downloading..."
    # we're using curl instead of wget because it will not
    # expose the sensitive uri in the build logs:
    curl -L -o ${ANDROID_SIGNING_KEY_SAVE_PATH} ${ANDROID_SIGNING_KEY_URI}
else
    echo "Keystore uri not set.  .APK artifact will not be signed."
fi
