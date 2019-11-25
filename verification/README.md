# Verification

This folder contains dummy maven projects and bash scripts to verify end-to-end functionality of the sonar-build-breaker plugin.

## Prerequisites
- Docker - the scripts run SonarQube as docker containers
- A bash shell - a given on Linux systems, but you can also use git bash on Windows.

## Semi-automatic verification

Can be used to test against a particular SonarQube version
and debug problems.

Verification steps:
- Build the sonar-build-breaker plugin
- Execute `./run_sonar_with_plugin.sh [sonarqube version]` to start a local Sonarqube docker container with installed sonar-build-breaker plugin. Without `[sonarqube version]` the `lts` version is used.
- Navigate to the `failing-project` and `passing-project` folders respectively and execute `mvn sonar:sonar` to run an analysis against local Sonarqube.
- As the names suggest, `passing-project` should pass the quality gate and SHOULD NOT trigger the builder breaker. `failing-project` has vulnerability issues
and SHOULD trigger the build breaker.

The dummy projects are based on https://github.com/SonarSource/sonar-scanning-examples/tree/master/sonarqube-scanner-maven/maven-basic.

## Automatic verification

Can be used to run the dummy projects against the set of supported sonarqube versions and automatically verify that everything works. Warning: this is slow.

Verification steps:
- Execute `./test_e2e.sh` and wait. Various log files for the steps are written to a `results` folder.