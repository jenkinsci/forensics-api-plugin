# Forensics API Jenkins Plugin

[![Join the chat at https://gitter.im/jenkinsci/warnings-plugin](https://badges.gitter.im/jenkinsci/warnings-plugin.svg)](https://gitter.im/jenkinsci/warnings-plugin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Jenkins Version](https://img.shields.io/badge/Jenkins-2.121.1-green.svg)](https://jenkins.io/download/)
![JDK8](https://img.shields.io/badge/jdk-8-yellow.svg)
[![License: MIT](https://img.shields.io/badge/license-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![GitHub pull requests](https://img.shields.io/github/issues-pr/uhafner/forensics-api-plugin.svg)](https://github.com/uhafner/forensics-api-plugin/pulls)

This Forensics API Jenkins plug-in defines an API to mine and analyze data from a repository. Currently, this plugin is only used
by the [Jenkins Warning Next Generation Plugin](https://github.com/jenkinsci/warnings-ng-plugin). 
[Jenkins Git Forensics Plugin](https://github.com/jenkinsci/git-forensics-plugin) is a plugin that implements the 
corresponding extension points for Git. Other version control systems are not supported yet.  

The following services are provided by this plugin:
- **Blames**: Shows what revision and author last modified a specified set of lines of a file.
- **File statistics**: Collects commit statistics for all repository files:
    - total number of commits
    - total number of different authors
    - creation time
    - last modification time

[![Travis](https://img.shields.io/travis/uhafner/forensics-api-plugin.svg?logo=travis&label=travis%20build&logoColor=white)](https://travis-ci.org/uhafner/forensics-api-plugin)
[![Codacy](https://api.codacy.com/project/badge/Grade/6f1e586841f7419bb40973862c8871aa)](https://www.codacy.com/app/uhafner/forensics-api-plugin?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=uhafner/forensics-api-plugin&amp;utm_campaign=Badge_Grade)
[![Codecov](https://img.shields.io/codecov/c/github/uhafner/forensics-api-plugin.svg)](https://codecov.io/gh/uhafner/forensics-api-plugin)
