#!/bin/bash
#
# Copyright (C) 2020 Mike Hummel (mh@mhus.de)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# config

REPO_PATH_ZIP="org/summerclouds/conductor-launcher/±project_version±/conductor-launcher-±project_version±-install.zip"
LOCAL_REPO_PATH_ZIP="$HOME/.m2/repository/$REPO_PATH_ZIP"
REMOTE_REPO_PATH_ZIP="https://repo1.maven.org/maven2/$REPO_PATH_ZIP"

# init

if [ ! -d $HOME/.conductor/bin/±project_version± ]; then
  mkdir -p $HOME/.conductor/bin/±project_version±
fi
if [ ! -d $HOME/.conductor/tmp ]; then
  mkdir -p $HOME/.conductor/tmp
fi

# download

if [ ! -e $LOCAL_REPO_PATH_ZIP ]; then
  if command -v mvn &> /dev/null; then
    mvn org.apache.maven.plugins:maven-dependency-plugin:3.1.2:get \
      -Dartifact=org.summerclouds:conductor-launcher:±project_version±:zip:install
    mvn dependency:get -DartifactId=conductor-plugin \
      -DgroupId=org.summerclouds -Dversion=±project_version± -Dpackaging=yml -Dclassifier=configuration-default -DrepoUrl=

  elif command -v curl &> /dev/null; then
    if [ -e $HOME/.conductor/tmp/con-install.zip ]; then
      rm $HOME/.conductor/tmp/con-install.zip
    fi
    curl --output $HOME/.conductor/tmp/con-install.zip $REMOTE_REPO_PATH_ZIP
    LOCAL_REPO_PATH_ZIP=$HOME/.conductor/tmp/con-install.zip
  else
     echo "Either mvn nor curl found - exit"
     exit 1
  fi
fi

if [ ! -e $LOCAL_REPO_PATH_ZIP ]; then
  echo "Can't download conductor install zip"
  echo $REMOTE_REPO_PATH_ZIP
  exit 1
fi

# unpack and setup

cd $HOME/.conductor/bin/±project_version±
unzip -o $LOCAL_REPO_PATH_ZIP
chmod +x $HOME/.conductor/bin/±project_version±/*.sh

if [ -e $HOME/.conductor/bin/con ]; then
  rm $HOME/.conductor/bin/con
fi
ln -s $HOME/.conductor/bin/±project_version±/con.sh $HOME/.conductor/bin/con

# cleanup

if [ -e $HOME/.conductor/tmp/con-install.zip ]; then
  rm $HOME/.conductor/tmp/con-install.zip
fi

echo "Installed ±project_version± in $HOME/.conductor"
echo "Add directory $HOME/.conductor/bin to \$PATH (export PATH=$PATH:$HOME/.conductor/bin/con) or link $HOME/.conductor/bin/con in a binary directory like /usr/local/bin"
