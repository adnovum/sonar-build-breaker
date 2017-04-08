# SonarQube Build Breaker Plugin

[![Build Status](https://api.travis-ci.org/SonarQubeCommunity/sonar-build-breaker.svg)](https://travis-ci.org/SonarQubeCommunity/sonar-build-breaker) [![SonarQube.com Quality Gate status](https://sonarqube.com/api/badges/gate?key=org.sonarqubecommunity.buildbreaker%3Asonar-build-breaker-plugin)](https://sonarqube.com/overview?id=org.sonarqubecommunity.buildbreaker%3Asonar-build-breaker-plugin)

## Compatibility

| SonarQube Version | Plugin Version(s) |
|-------------------|-------------------|
| 1.12 - 2.14       | 1.0 |
| 3.0 - 5.1         | 1.0, 1.1 |
| 5.2               | Not compatible due to limitations with SonarQube platform |
| 5.3 - 5.4         | 2.0, 2.1, 2.2 |
| 5.5 - 6.3         | 2.1, 2.2 |

## Download and Installation

1. Download the appropriate `sonar-build-breaker-plugin-${version}.jar` file from the [releases page](https://github.com/SonarQubeCommunity/sonar-build-breaker/releases), based on the compatibility chart
2. Copy the plugin into `/path/to/sonarqube/extensions/plugins/`
3. Remove older versions of the plugin from `/path/to/sonarqube/extensions/plugins/`, if present
4. Restart SonarQube

## Description

This plugin will mark the build failed if the project fails its quality gate or uses a forbidden configuration.  These
checks happen after analysis has been submitted to the server, so it does not prevent a new analysis from showing up in
SonarQube.

Upon uploading the analysis information, the plugin follows the below workflow to check the quality gate:

1. Search `${sonar.working.directory}/report-task.txt` for `ceTaskId`, the server-side Compute Engine (CE) task associated with the current analysis
2. Call the `${sonar.host.url}/api/ce/task?id=${ceTaskId}` web service to retrieve `analysisId`
  1. If the CE Task Status is `PENDING` or `IN_PROGRESS`, wait `sonar.buildbreaker.queryInterval` and repeat step 2
  2. If the CE Task Status is `SUCCESS`, save the `analysisId` and proceed to step 3
  3. If the CE Task Status is `FAILED` or none of the above, break the build
  4. If step 2 has been attempted `sonar.buildbreaker.queryMaxAttempts` times, break the build
3. Call the `${sonar.host.url}/api/qualitygates/project_status?analysisId=${analysisId}` web service to check the status of the quality gate
  1. If the quality gate status is `OK`, allow the build to pass
  2. If the quality gate status is `WARN`, allow the build to pass and log the current warnings
  3. If the quality gate status is `ERROR`, break the build and log the current warnings and errors

The build "break" is accomplished by throwing an exception, making the analysis return with a non-zero status code.
This allows you to benefit from the notifications built into CI engines or use your own custom notifications that check the
exit status.

## Usage

### Quality Gate Build Breaker

This build breaker is only available in publish mode (`sonar.analysis.mode=publish`, the default).
SonarQube calculates the quality gate status server-side, and only publish mode sends analysis results
to the server.

1. Associate a quality gate to your project
2. Optional: Tune `sonar.buildbreaker.queryMaxAttempts` and/or `sonar.buildbreaker.queryInterval`
  1. Check the duration of previous CE (background) tasks for your project, from submission until completion
  2. Ensure `sonar.buildbreaker.queryMaxAttempts * sonar.buildbreaker.queryInterval` is longer than the above duration (with default values, total wait time is ~5 minutes)
  3. For small projects, a faster interval may be desired so your build times are not longer than necessary
  4. For very large projects or servers with a busy CE queue, more attempts or a longer interval may be necessary
3. Run an analysis on your project
4. If analysis fails while waiting for CE to complete, increase either `sonar.buildbreaker.queryMaxAttempts`, `sonar.buildbreaker.queryInterval`, or both

### Forbidden Configuration Build Breaker

Define the property `sonar.buildbreaker.forbiddenConf` with comma-separated `key=value` configurations that will break
the build.

For example, if you set the property to `sonar.gallio.mode=skip`, each analysis on .NET projects executed with
Gallio skipped will be marked "broken".

### Issues Severity Build Breaker

This build breaker is only available in preview modes (`sonar.analysis.mode=preview` or
`sonar.analysis.mode=issues`).  In publish mode, issue severity data is not available to post-job
checkers.

1. Define the property `sonar.buildbreaker.preview.issuesSeverity`
2. Define `sonar.analysis.mode=preview`
3. Ensure `sonar.preview.excludePlugins` does not have `buildbreaker` in the list (prior to SonarQube 6.2, it is in the list by default)

### Configuration Parameters

| Property | Description | Default value | Example |
| -------- | ----------- | ------------- | ------- |
| `sonar.buildbreaker.skip` | If set to true, the quality gate is not checked.  By default the build will break if the project does not pass the quality gate. | `false` | |
| `sonar.buildbreaker.queryMaxAttempts` | The maximum number of queries to the API when waiting for report processing.  The build will break if this is reached.  Total wait time is `sonar.buildbreaker.queryMaxAttempts * sonar.buildbreaker.queryInterval`. | `30` | |
| `sonar.buildbreaker.queryInterval` | The interval (ms) between queries to the API when waiting for report processing.  Total wait time is `sonar.buildbreaker.queryMaxAttempts * sonar.buildbreaker.queryInterval`. | `10000` | |
| `sonar.buildbreaker.forbiddenConf` | Comma-separated list of `key=value` pairs that should break the build. | | `sonar.gallio.mode=skip` |
| `sonar.buildbreaker.alternativeServerUrl` | URL to use for web service requests. If unset, uses the `serverUrl` property from `${sonar.working.directory}/report-task.txt`. | | |
| `sonar.buildbreaker.preview.issuesSeverity` | Fails the build in preview analysis mode if the severity of issues is equal to or more severe than the selection. | `Disabled` | Available selections are (case insensitive): `Disabled`, `INFO`, `MINOR`, `MAJOR`, `CRITICAL`, `BLOCKER` |

## Contributing

When reporting issues, please include complete steps to reproduce the issue and all relevant logs.

Pull requests are welcome, but may not always be reviewed immediately.  Please be patient while
waiting for review.  This project is maintained on a best-effort basis (aka during spare time).

### Pull Request Guidelines

1. This project uses [google-java-format](https://github.com/google/google-java-format), so please
   ensure all Java code is formatted with this tool
2. For the most part, the project follows standard Oracle Java code conventions
3. Include unit tests
   1. Do not use PowerMock unless there is no alternative
   2. Keep project coverage above 90%
4. Update the documentation (this `README.md`) with new configuration parameters and usage notes
5. Make sure your change works with all versions of SonarQube starting at the minimum version
   defined in `pom.xml`
   1. If you need to upgrade the base SonarQube version, create an issue for discussion first
   2. Once upgraded, the base version will not be downgraded
