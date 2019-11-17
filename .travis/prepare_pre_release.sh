#!/usr/bin/env bash
set -eu
PRE_RELEASE_TAG=travis-pre-release

# Delete the existing pre release, otherwise due to the dynamic versions (using build number)
# travis will continually add new artifacts to the existing pre-release.
wget https://github.com/github/hub/releases/download/v2.13.0/hub-linux-amd64-2.13.0.tgz
tar xf hub-linux-amd64-2.13.0.tgz
if hub-linux-amd64-2.13.0/bin/hub release | grep $PRE_RELEASE_TAG >/dev/null; then
    hub-linux-amd64-2.13.0/bin/hub release delete $PRE_RELEASE_TAG 
fi

# Update the pre release tag
git tag -f $PRE_RELEASE_TAG
git remote add gh https://${TRAVIS_REPO_SLUG%/*}:${GITHUB_TOKEN}@github.com/${TRAVIS_REPO_SLUG}.git
git push -f gh $PRE_RELEASE_TAG
git remote remove gh
