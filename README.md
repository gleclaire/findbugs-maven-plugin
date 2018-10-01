# Findbugs Maven Plugin


[![Build Status](https://travis-ci.org/gleclaire/findbugs-maven-plugin.svg?branch=master)](https://travis-ci.org/gleclaire/findbugs-maven-plugin)
[![Maven central](https://maven-badges.herokuapp.com/maven-central/org.codehaus.mojo/findbugs-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.codehaus.mojo/findbugs-maven-plugin)
[![Apache 2](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

## Status ##

**Note: ** Since Findbugs is no longer maintained, please use Spotbugs which has a Maven plugin. It is located [here](https://spotbugs.github.io/)

## Latest Snapshot ##

Please download latest snapshots from [here](https://oss.sonatype.org/content/repositories/snapshots/org/codehaus/mojo/findbugs-maven-plugin/)

Building findbugs-maven-plugin Requirements
=====================

Java 7 is required.  This will allow analysis of bytecode up to Java 8

findbugs-maven-plugin
=====================

Maven Mojo Plug-In to generate reports based on the FindBugs Analyzer

Run all tests
```
mvn -DtestSrc=remote -Prun-its clean install -D"invoker.parallelThreads=4"
```
Skip tests
```
mvn -DskipTests=true clean install
```
Run tests on findbugs test source code that is local instead of from FindBugs github repository
```
mvn -DtestSrc=local -DlocalTestSrc=/opt/findBugs -Prun-its clean install -D"invoker.parallelThreads=4"
```

Run selected tests
```
mvn -DtestSrc=remote -Prun-its -Dinvoker.test=build-*,basic-1,check-nofail clean install -D"invoker.parallelThreads=4"
```

Run tests in debugger
```
mvn -Dmaven.surefire.debug="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -Xnoagent -Djava.compiler=NONE" -Prun-its clean install 
```

Run selected tests in debugger
```
mvn -Dmaven.surefire.debug="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -Xnoagent -Djava.compiler=NONE" -Prun-its -Dinvoker.test=build-*,basic-1,check clean install
```

Run gui with a specific version 
```
mvn org.codehaus.mojo:findbugs-maven-plugin:3.0.6-SNAPSHOT:gui 
```
