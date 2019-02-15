sudo apt install python-pip python-dev
sudo pip install awscli
aws s3 cp s3://uhp-android-app/release-key.jks app/release-key.jks
aws s3 cp s3://uhp-android-app/google-play-key.json app/google-play-key.json
aws s3 cp s3://uhp-android-app/claims_submission.variables.gradle variables.gradle
