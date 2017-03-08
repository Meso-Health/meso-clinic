if [ $1 == 'sandbox' ]; then
    echo "Sandbox environment variables set in copyEnvVariablesToSecrets"
    ROLLBAR_API_KEY=${ROLLBAR_API_KEY}
    PROVIDER_ID=${PROVIDER_ID}
    API_HOST=${SANDBOX_API_HOST}
    ROLLBAR_ENV_KEY=${SANDBOX_ROLLBAR_ENV_KEY}
    SIMPRINTS_API_KEY=${SANDBOX_SIMPRINTS_API_KEY}
    HOCKEYAPP_APP_ID=${SANDBOX_HOCKEYAPP_APP_ID}
else
    echo "No production environment variables are set yet."
fi

SECRET_XML_PATH="app/src/main/res/xml/secret.xml"

echo "<?xml version=\"1.0\" encoding=\"utf-8\"?>" > ${SECRET_XML_PATH}
echo "<secret>" >> ${SECRET_XML_PATH}
echo "    <entry key=\"ROLLBAR_API_KEY\">${ROLLBAR_API_KEY}</entry>" >> ${SECRET_XML_PATH}
echo "    <entry key=\"API_HOST\">${API_HOST}</entry>" >> ${SECRET_XML_PATH}
echo "    <entry key=\"PROVIDER_ID\">${PROVIDER_ID}</entry>" >> ${SECRET_XML_PATH}
echo "    <entry key=\"ROLLBAR_ENV_KEY\">${ROLLBAR_ENV_KEY}</entry>" >> ${SECRET_XML_PATH}
echo "    <entry key=\"SIMPRINTS_API_KEY\">${SIMPRINTS_API_KEY}</entry>" >> ${SECRET_XML_PATH}
echo "    <entry key=\"HOCKEYAPP_APP_ID\">${HOCKEYAPP_APP_ID}</entry>" >> ${SECRET_XML_PATH}
echo "</secret>" >> ${SECRET_XML_PATH}

GRADLE_PROPERTIES_PATH="app/gradle.properties"