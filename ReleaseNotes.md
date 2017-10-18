# v0.2 (10/20/2017)
# Release Notes

## Notable Changes
The Barcelona Release (v 0.2) of the Core Command micro service includes the following:
* Application of Google Style Guidelines to the code base
* Increase in unit/intergration tests from 28 tests to 60 tests
* POM changes for appropriate repository information for distribution/repos management, checkstyle plugins, etc.
* Removed all references to unfinished DeviceManager work as part of Dell Fuse
* Added Dockerfile for creation of micro service targeted for ARM64 
* Added interfaces for all Controller classes

## Bug Fixes
* Removed OS specific file path for logging file 
* Provide option to include stack trace in log outputs

## Pull Request/Commit Details
 - [#15](https://github.com/edgexfoundry/core-command/pull/15) - Remove staging plugin contributed by Jeremy Phelps ([JPWKU](https://github.com/JPWKU))
 - [#14](https://github.com/edgexfoundry/core-command/pull/14) - Fixes Maven artifact dependency path contributed by Tyler Cox ([trcox](https://github.com/trcox))
 - [#13](https://github.com/edgexfoundry/core-command/pull/13) - added staging and snapshots repos to pom along with nexus staging mav… contributed by Jim White ([jpwhitemn](https://github.com/jpwhitemn))
 - [#12](https://github.com/edgexfoundry/core-command/pull/12) - Removed device manager url refs in properties files contributed by Jim White ([jpwhitemn](https://github.com/jpwhitemn))
 - [#11](https://github.com/edgexfoundry/core-command/pull/11) - Add aarch64 docker file contributed by ([feclare](https://github.com/feclare))
 - [#10](https://github.com/edgexfoundry/core-command/pull/10) - pom updated for checkstyle and nexus repos, LogErrorController update… contributed by Jim White ([jpwhitemn](https://github.com/jpwhitemn))
 - [#9](https://github.com/edgexfoundry/core-command/pull/9) - Typo fix contributed by Soumya Kanti Roy chowdhury ([soumyakantiroychowdhury](https://github.com/soumyakantiroychowdhury))
 - [#8](https://github.com/edgexfoundry/core-command/pull/8) - Updates Alpine image version contributed by Tyler Cox ([trcox](https://github.com/trcox))
 - [#7](https://github.com/edgexfoundry/core-command/pull/7) - change in tests to use new metadata packaging contributed by Jim White ([jpwhitemn](https://github.com/jpwhitemn))
 - [#6](https://github.com/edgexfoundry/core-command/pull/6) - fixed unit test broken by core-test enum change for google stlyes. A… contributed by Jim White ([jpwhitemn](https://github.com/jpwhitemn))
 - [#5](https://github.com/edgexfoundry/core-command/pull/5) - Adds Docker build capability contributed by Tyler Cox ([trcox](https://github.com/trcox))
 - [#4](https://github.com/edgexfoundry/core-command/pull/4) - Fixes Log File Path contributed by Tyler Cox ([trcox](https://github.com/trcox))
 - [#3](https://github.com/edgexfoundry/core-command/issues/3) - Log File Path not Platform agnostic
 - [#2](https://github.com/edgexfoundry/core-command/pull/2) - Add distributionManagement for artifact storage contributed by Andrew Grimberg ([tykeal](https://github.com/tykeal))
 - [#1](https://github.com/edgexfoundry/core-command/pull/1) - Contributed Project Fuse source code contributed by Tyler Cox ([trcox](https://github.com/trcox))
