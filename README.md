# Jenkins Plugin for IntelliJ IDEA
## with Patch Parameter Plugin Support (Pre-tested commit)
(https://wiki.jenkins-ci.org/display/JENKINS/Patch+Parameter+Plugin)

Information about pre-tested commit: https://wiki.jenkins-ci.org/display/JENKINS/Designing+pre-tested+commit

## Version 0.8.1.1-ppp-SNAPSHOT

If you want to test the latest features, you can download the [latest snapshot](https://github.com/nyver/jenkins-control-plugin/blob/master/snapshot/jenkins-control-plugin-0.8.1-SNAPSHOT-distribution.zip?raw=true).

To install it, **Settings -> Plugins -> Install from disk** and restart your IDE.

### Last features
* Added support of Patch Parameter Plugin for pre-tested commit

## Important Notes for the version >= 0.8.0
This release is not compatible with IDEA 10 anymore.

## ChangeLog
See CHANGELOG.txt

## Original plugin
https://github.com/dboissier/jenkins-control-plugin

## Description
This plugin allows to view the content of your Jenkins Continous Integration Server.

![Browser](https://github.com/dboissier/jenkins-control-plugin/blob/master/doc/images/Browser.png?raw=true)

![Upload](https://github.com/nyver/jenkins-control-plugin/blob/master/doc/howto/2_setup_ide/04.png?raw=true)

## Plugin Compatibility
This plugin was built with JDK 1.6 and IDEA 11 version. Jenkins CIs of jenkins-ci and apache.org are used for manual and stress testing.

## Build with Maven

* mvn install:install-file -Dfile=idea.jar -DgroupId=com.intellij -DartifactId=idea -Dversion=11.1.4 -Dpackaging=jar -DgeneratePom=true
* mvn install:install-file -Dfile=openapi.jar -DgroupId=com.intellij -DartifactId=openapi -Dversion=11.1.4 -Dpackaging=jar -DgeneratePom=true
* mvn install:install-file -Dfile=forms_rt.jar -DgroupId=com.intellij -DartifactId=forms_rt -Dversion=11.1.4 -Dpackaging=jar -DgeneratePom=true

mvn clean package -Dmaven.test.skip=true

## Limitations
* This software is written under Apache License 2.0.
* if Jenkins is behing an HTTPS web server, set a **trusted** certificate.
