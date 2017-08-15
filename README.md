[![Build Status](https://travis-ci.org/gleclaire/findbugs-maven-plugin.svg?branch=master)](https://travis-ci.org/gleclaire/findbugs-maven-plugin)

# **Note:**  CodeHaus has been taken off-line.

Snapshots of the plugin are now located in the Sonatype Repository at https://oss.sonatype.org/content/repositories/snapshots/org/codehaus/mojo/findbugs-maven-plugin/

Building findbugs-maven-plugin Requirements
=====================

Java 7 is required.  This will allow analysis of bytecode up to Java 8



findbugs-maven-plugin
=====================

Maven Mojo Plug-In to generate reports based on the FindBugs Analyzer

Run all tests
```
mvn -DtestSrc=remote -Prun-its clean install
```
Skip tests
```
mvn -DskipTests=true clean install
```
Run tests on findbugs test source code that is local instead of from FindBugs github repository
```
mvn -DtestSrc=local -DlocalTestSrc=/opt/findBugs -Prun-its clean install
```

Run selected tests
```
mvn -Prun-its -Dinvoker.test=build-*,basic-1,check clean install
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
mvn org.codehaus.mojo:findbugs-maven-plugin:3.0.2-SNAPSHOT:gui 
```
