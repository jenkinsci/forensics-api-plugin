# Forensics API Jenkins Plugin

[![Join the chat at https://gitter.im/jenkinsci/warnings-plugin](https://badges.gitter.im/jenkinsci/warnings-plugin.svg)](https://gitter.im/jenkinsci/warnings-plugin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/forensics-api.svg)](https://plugins.jenkins.io/forensics-api)
[![Jenkins Version](https://img.shields.io/badge/Jenkins-2.138.4-green.svg)](https://jenkins.io/download/)
![JDK8](https://img.shields.io/badge/jdk-8-yellow.svg)
[![License: MIT](https://img.shields.io/badge/license-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![GitHub pull requests](https://img.shields.io/github/issues-pr/jenkinsci/forensics-api-plugin.svg)](https://github.com/jenkinsci/forensics-api-plugin/pulls)

This Forensics API Jenkins plug-in defines an API to mine and analyze data from a source control repository. Currently, this plugin is only used
by the [Jenkins Warning Next Generation Plugin](https://github.com/jenkinsci/warnings-ng-plugin).
 
The API of the following services is defined by this plugin:
- **Blames**: Shows what revision and author last modified a specified set of lines of a file.
- **File statistics**: Collects commit statistics for repository files:
    - total number of commits
    - total number of different authors
    - creation time
    - last modification time

## Implementations

[Jenkins Git Forensics Plugin](https://github.com/jenkinsci/git-forensics-plugin) is a plugin that implements the 
corresponding extension points for Git. Other version control systems are not yet supported.  

[![Jenkins](https://ci.jenkins.io/job/Plugins/job/forensics-api-plugin/job/master/badge/icon)](https://ci.jenkins.io/job/Plugins/job/forensics-api-plugin/job/master/)
[![Travis](https://img.shields.io/travis/jenkinsci/forensics-api-plugin/master.svg?logo=travis&label=travis%20build&logoColor=white)](https://travis-ci.org/jenkinsci/forensics-api-plugin)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/1be7bb5b899446968e411e6e59c8ea6c)](https://www.codacy.com/app/uhafner/forensics-api-plugin?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=jenkinsci/forensics-api-plugin&amp;utm_campaign=Badge_Grade)
[![Codecov](https://img.shields.io/codecov/c/github/jenkinsci/forensics-api-plugin.svg)](https://codecov.io/gh/jenkinsci/forensics-api-plugin)
