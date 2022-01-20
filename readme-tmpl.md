
# Project Conductor

## Purposed

The Conductor is created to handle multiple projects.

Define projects and lifecycle steps to process actions.

The {{project.version}} Version.

## Install

Install using the install script from maven central

```
curl https://repo1.maven.org/maven2/de/mhus/conductor/conductor-launcher/{{project.version}}/conductor-launcher-{{project.version}}-install.sh | bash

or with wget:

wget -qO- https://repo1.maven.org/maven2/de/mhus/conductor/conductor-launcher/{{project.version}}/conductor-launcher-{{project.version}}-install.sh | bash

```

The script will install the conductor binaries in $HOME/.conductor and link $HOME/.conductor/bin/con to the current installed version.

You could use the link directly with 

```
$HOME/.conductor/bin/con -help
```

or include it in the $PATH variabe to execute it without using the directory. e.g. link it into /usr/local/bin (only one time)

```
ln -s $HOME/.conductor/bin/con /usr/local/bin/con
```

