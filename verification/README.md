# Verification

This folder contains dummy maven projects to verify end-to-end functionality of the sonar-build-breaker plugin.

Verification steps:
- Build the sonar-build-breaker plugin
- Execute `run_sonar_with_plugin.sh` to start a local Sonarqube docker container with installed sonar-build-breaker plugin
- Navigate to the `failing-project` and `passing-project` folders respectively and execute `mvn sonar:sonar` to run an analysis against local Sonarqube.
- As the names suggest, `passing-project` should pass the quality gate and SHOULD NOT trigger the builder breaker. `failing-project` has vulnerability issues
and SHOULD trigger the build breaker.

The dummy projects are based on https://github.com/SonarSource/sonar-scanning-examples/tree/master/sonarqube-scanner-maven/maven-basic.
