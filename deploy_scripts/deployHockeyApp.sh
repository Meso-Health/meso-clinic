# Deploys the signed APK to hockeyapp.

# Create the notes
GIT_COMPARE_KEY=${CIRCLE_COMPARE_URL##*/}
GIT_PRETTY_COMMIT_LOG=$(echo "<ul>$(git log ${GIT_COMPARE_KEY} --pretty=format:'<li>[%ad] %s (%an)</li>' --date=short)</ul>" | tr -d '\n')
HOCKEYAPP_NOTES_HEADER="**Built on:** $(date +"%a %d-%b-%Y %I:%M %p")
**Branch:** $(git rev-parse --abbrev-ref HEAD)
**Commit:** $(git rev-parse --short HEAD)"
HOCKEYAPP_NOTES_HEADER_HTML=${HOCKEYAPP_NOTES_HEADER//$'\n'/<br>}
HOCKEYAPP_NOTES="${HOCKEYAPP_NOTES_HEADER_HTML} ${GIT_PRETTY_COMMIT_LOG}"

echo ${HOCKEYAPP_NOTES}
echo "Environment variables set to $1 in deployHockeyApp.sh"
if [ $1 == 'sandbox' ]; then
    HOCKEYAPP_ACCESS_TOKEN=${SANDBOX_HOCKEYAPP_ACCESS_TOKEN}
    HOCKEYAPP_APP_ID=${SANDBOX_HOCKEYAPP_APP_ID}
    HOCKEYAPP_EXPORT_APK_PATH="app/build/outputs/apk/app-sandbox-release.apk"
elif [ $1 == 'demo' ]; then
    HOCKEYAPP_ACCESS_TOKEN=${DEMO_HOCKEYAPP_ACCESS_TOKEN}
    HOCKEYAPP_APP_ID=${DEMO_HOCKEYAPP_APP_ID}
    HOCKEYAPP_EXPORT_APK_PATH="app/build/outputs/apk/app-demo-release.apk"
elif [ $1 == 'training' ]; then
    HOCKEYAPP_ACCESS_TOKEN=${TRAINING_HOCKEYAPP_ACCESS_TOKEN}
    HOCKEYAPP_APP_ID=${TRAINING_HOCKEYAPP_APP_ID}
    HOCKEYAPP_EXPORT_APK_PATH="app/build/outputs/apk/app-training-release.apk"
    HOCKEYAPP_NOTES=""
elif [ $1 == 'production' ]; then
    HOCKEYAPP_ACCESS_TOKEN=${PRODUCTION_HOCKEYAPP_ACCESS_TOKEN}
    HOCKEYAPP_APP_ID=${PRODUCTION_HOCKEYAPP_APP_ID}
    HOCKEYAPP_EXPORT_APK_PATH="app/build/outputs/apk/app-production-release.apk"
    HOCKEYAPP_NOTES=""
else
    echo "Invalid argument passed into deployHockeyApp.sh. Argument was: $1"
    echo "Exiting script."
    exit;
fi

curl --verbose \
     --fail \
     --form "ipa=@${HOCKEYAPP_EXPORT_APK_PATH}" \
     --form "notes=${HOCKEYAPP_NOTES}" \
     --form "notes_type=0" \
     --form "notify=1" \
     --form "status=2" \
     --form "platform=Android" \
     --header "X-HockeyAppToken: ${HOCKEYAPP_ACCESS_TOKEN}" \
     "https://upload.hockeyapp.net/api/2/apps/${HOCKEYAPP_APP_ID}/app_versions/upload"
