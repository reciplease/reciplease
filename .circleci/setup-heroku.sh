#!/bin/bash

set -eu

HEROKU_APP_NAME=$1
HEROKU_LOGIN=$2
HEROKU_API_KEY=$3

git remote add heroku https://git.heroku.com/$HEROKU_APP_NAME.git
wget https://cli-assets.heroku.com/branches/stable/heroku-linux-amd64.tar.gz

sudo mkdir -p /usr/local/lib /usr/local/bin
sudo tar -xzf heroku-linux-amd64.tar.gz -C /usr/local/lib
sudo ln -s /usr/local/lib/heroku/bin/heroku /usr/local/bin/heroku

cat > ~/.netrc << EOF
machine api.heroku.com
	login $HEROKU_LOGIN
	password $HEROKU_API_KEY
machine git.heroku.com
	login $HEROKU_LOGIN
	password $HEROKU_API_KEY
EOF

chmod 600 ~/.netrc

# Add heroku.com to the list of known hosts
ssh-keyscan -H heroku.com >> ~/.ssh/known_hosts