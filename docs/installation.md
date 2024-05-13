# Installation

## Simple

The simple way to install conductor on a unixoide os with wget and bash
is to execute the following command:

    version=<latest>
    wget -qO- https://repo1.maven.org/maven2/de/mhus/conductor/conductor-launcher/${version}/conductor-launcher-${version}-install.sh | bash

This will install the selected version of conductor in you
$HOME/.conductor directory.

Conductor is able to install multiple versions in the same time.

Now you have to link the default version of conductor into a bin folder
or add the directory to your $PATH variable. Then it is possible to use
the conductor command *con*.

For example link the command to /usr/local/bin

    sudo ln -s $HOME/.conductor/bin/con /usr/local/bin

## Build from source

Clone the project from github. You need to download a tag version to
fulfill all dependencies in release status.

    git clone https://github.com/mhus/conductor.git -b <version tag>
    cd mhus-conductor

The script *./create.sh* will build and install the selected version of
conductor.
