sudo apt install python-pip python-dev
sudo pip install awscli
aws s3 cp s3://meso-secrets/release-key.jks app/release-key.jks
aws s3 cp s3://meso-secrets/google-play-key.json app/google-play-key.json
aws s3 cp s3://meso-secrets/ethiopia.claims_submission.variables.gradle variables.gradle
