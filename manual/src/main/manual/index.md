## Overview

Conductor is a tool to manage multiple connected software projects
at the same time.

Projects are connected if they depend on another projects and they are
in snapshot status in the same time. Usually parent and extending
projects.

Managing projects means simple actions like build or rebuild and also
complex processes like deployment. Actions can be executed on all or
selected projects with one command.

For example
* Create a git branch for all projects
* add, commit and push all projects in the same time
* build or rebuild all projects in correct order with maven

It's designed to process complex release deploy processes creating new versions,
tags and switch to the next SNAPSHOT version.

In the conductor configurations you define actions as lifecycles. Each lifecycle is
a list of steps. Conductor can even help you manage single projects.
