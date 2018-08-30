#!/usr/bin/env bash
set -ex

releaseOptions="-DautoVersionSubmodules -DpushChanges=false -DlocalCheckout"

echo "About to run the release builds."
echo "You will need to confirm the versions and tag."
echo "After that you may need to enter your GPG pass-phrase twice."

if ./mvnw release:prepare release:perform ${releaseOptions}; then
  # NB This line will fail the script if the 2nd last commit is not a tag
  tag=$(git describe --exact-match --abbrev=0 HEAD~1)
  echo "Maven release deployment succeeded."
  echo "Please visit Sonatype OSS to close, release and drop the staging repository:"
  echo "      https://oss.sonatype.org/#stagingRepositories"
  echo
  # NB: Maven Central validation might fail
  echo "Note: if the release fails, you should run this to clean up:"
  echo "      git tag --delete ${tag} && git reset --hard HEAD~2 && ./mvnw release:clean"
  echo
  read -p "Press enter when the repository has been released..." -s
  echo
  echo "Pushing master branch and tag '${tag}' to github:"
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

  echo "You may need to delete the staging repository in Sonatype OSS."
  echo "      https://oss.sonatype.org/#stagingRepositories"
  echo
  echo "You should probably run this before trying again: ./mvnw release:clean"
fi
