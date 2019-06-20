# Forensics API Jenkins Plugin

[![Join the chat at https://gitter.im/jenkinsci/warnings-plugin](https://badges.gitter.im/jenkinsci/warnings-plugin.svg)](https://gitter.im/jenkinsci/warnings-plugin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Jenkins Version](https://img.shields.io/badge/Jenkins-2.121.1-green.svg)](https://jenkins.io/download/)
![JDK8](https://img.shields.io/badge/jdk-8-yellow.svg)
[![License: MIT](https://img.shields.io/badge/license-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![GitHub pull requests](https://img.shields.io/github/issues-pr/jenkinsci/forensics-api-plugin.svg)](https://github.com/jenkinsci/forensics-api-plugin/pulls)

This Forensics API Jenkins plug-in defines an API to mine and analyze data from a repository. Currently, this plugin is only used
by the [Jenkins Warning Next Generation Plugin](https://github.com/jenkinsci/warnings-ng-plugin).
 
The API of the following services is defined by this plugin:
- **Blames**: Shows what revision and author last modified a specified set of lines of a file.
- **File statistics**: Collects commit statistics for all repository files:
    - total number of commits
    - total number of different authors
    - creation time
    - last modification time

## Implementations

[Jenkins Git Forensics Plugin](https://github.com/jenkinsci/git-forensics-plugin) is a plugin that implements the 
corresponding extension points for Git. Other version control systems are not supported yet.  

[![Jenkins](https://ci.jenkins.io/job/Plugins/job/forensics-api-plugin/job/master/badge/icon)](https://ci.jenkins.io/job/Plugins/job/forensics-api-plugin/job/master/)
[![Travis](https://img.shields.io/travis/jenkinsci/forensics-api-plugin.svg?logo=travis&label=travis%20build&logoColor=white)](https://travis-ci.org/jenkinsci/forensics-api-plugin)
[![Codacy](https://api.codacy.com/project/badge/Grade/1b96405c72db49eeb0d67486f77f8f75)](https://app.codacy.com/app/uhafner/analysis-model?utm_source=github.com&utm_medium=referral&utm_content=jenkinsci/analysis-model&utm_campaign=Badge_Grade_Dashboard)
[![Codecov](https://img.shields.io/codecov/c/github/jenkinsci/forensics-api-plugin.svg)](https://codecov.io/gh/jenkinsci/forensics-api-plugin)
