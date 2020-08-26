# Jenkins Forensics API Plugin

[![Join the chat at https://gitter.im/jenkinsci/warnings-plugin](https://badges.gitter.im/jenkinsci/warnings-plugin.svg)](https://gitter.im/jenkinsci/warnings-plugin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/forensics-api.svg?label=latest%20version)](https://plugins.jenkins.io/forensics-api)
[![Jenkins](https://ci.jenkins.io/job/Plugins/job/forensics-api-plugin/job/master/badge/icon?subject=Jenkins%20CI)](https://ci.jenkins.io/job/Plugins/job/forensics-api-plugin/job/master/)
[![GitHub Actions](https://github.com/jenkinsci/forensics-api-plugin/workflows/GitHub%20CI/badge.svg?branch=master)](https://github.com/jenkinsci/forensics-api-plugin/actions)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/1be7bb5b899446968e411e6e59c8ea6c)](https://www.codacy.com/app/jenkinsci/forensics-api-plugin?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=jenkinsci/forensics-api-plugin&amp;utm_campaign=Badge_Grade)
[![Codecov](https://codecov.io/gh/jenkinsci/forensics-api-plugin/branch/master/graph/badge.svg)](https://codecov.io/gh/jenkinsci/forensics-api-plugin)

This Forensics API Jenkins plug-in defines an API to mine and analyze data from a source control repository. Currently, this plugin is only used
by the [Jenkins Warning Next Generation Plugin](https://github.com/jenkinsci/warnings-ng-plugin).
 
This API plugin provides the following services:
- **Blames**: Shows what revision and author last modified a specified set of lines of a file. This information can be 
used to track the original commit that introduced a piece of code. 
- **File statistics**: Collects commit statistics for all repository files in the style of 
  [Code as a Crime Scene](https://www.adamtornhill.com/articles/crimescene/codeascrimescene.htm) 
  \[Adam Tornhill, November 2013\]:
  - total number of commits
  - total number of different authors
  - creation time
  - last modification time
- **Commit tracking**: Tracks all new commits that are part of a build. With this information plugins can select the 
first build that contains a specific commit. 
- **Reference build**: Plugins that want to show information (delta reports) relative to another build 
need a way to select this so called reference build. A reference build can be a previous build for the same 
repository branch or if the project is working with several branches a specific build from the job that builds 
the target branch (i.e., the branch the current changes will be merged into). 
Examples for consumers of this reference build are:
   - The warnings plugin computes new or fixed static analysis warnings based on a comparison with the reference build.
   - The code coverage plugin calculates the coverage difference of the actual changes compared to the results 
   of the reference build.

## Implementations

[Jenkins Git Forensics Plugin](https://github.com/jenkinsci/git-forensics-plugin) is a plugin that implements the 
corresponding extension points for Git. Other version control systems are not yet supported.  


