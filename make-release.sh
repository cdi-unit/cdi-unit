#!/usr/bin/env bash
set -ex

releaseOptions="-DautoVersionSubmodules -DpushChanges=false -DlocalCheckout"

if ./mvnw release:prepare release:perform ${releaseOptions}; then
  echo "Release succeeded. Pushing branch and tag to github:"
  tag=$(git describe --exact-match --abbrev=0 HEAD~1)
  git push origin master ${tag}
else
  echo "release:prepare or release:perform failed."
  echo "Attempting to delete the release tag:"

  # NB This line will fail the script if the 2nd last commit is not a tag
  tag=$(git describe --exact-match --abbrev=0 HEAD~1)
  # (this returns 0 even if the tag is empty)
  git tag --delete ${tag}

  echo "Resetting the current branch to remove release commits:"
  # Assuming that the tag was found, it should be safe to reset the branch
  git reset --hard HEAD~2

  echo "You should probably run this before trying again: ./mvnw release:clean"
fi
