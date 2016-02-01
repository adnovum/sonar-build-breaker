Build Breaker Plugin
====================

Download and Version information: http://update.sonarsource.org/plugins/buildbreaker-confluence.html

## Description / Features
This plugin will mark the build failed if the project fails its quality gate. It is to be noted that the analysis does not stop if the quality gate fails; it continues successfully to the end. But the build status will report failure.
This plugin allows you benefit from the notifications built into CI engines.

## Usage
1. Associate a quality gate to your project
2. Run a quality analysis on your project

### Advanced Usage
By default this plugin is active on every project. But you can skip its execution on some of them by setting the sonar.buildbreaker.skip property to true at the project level. This property can also be set globally, so that it is off by default, but on for individual projects.
The property sonar.buildbreaker.forbidden.conf can be used to specify configurations that would break the build. For example, if you set in the sonar.buildbreaker.forbidden.conf property to sonar.gallio.mode=skip, each analysis on .NET projects executed with Gallio skipped would be marked "broken".

### Build status

[![Build Status](https://api.travis-ci.org/SonarQubeCommunity/sonar-build-breaker.svg)](https://travis-ci.org/SonarQubeCommunity/sonar-build-breaker)
