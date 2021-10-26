#!/usr/bin/env bash
# file: git-init.sh
# description: Configure repository to allow commiting. Used by CI.

git config --global user.name 'Github Actions'
git config --global user.email 'github-actions@users.noreply.github.com'
          