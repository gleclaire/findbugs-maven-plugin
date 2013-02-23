findbugs-maven-plugin
=====================

Maven Mojo Plug-In to generate reports based on the FindBugs Analyzer

The documentation for the **FindBugs Maven Plugin** is here: http://mojo.codehaus.org/findbugs-maven-plugin/


Run all test
mvn -Prun-its clean install

Skip tests
mvn -DskipTests=true clean install

Run tests on findbugs test source code that is local instead of from FindBugs SVN repository
mvn -o -DtestSrc=local -DlocalFindbugTestSrc=/opt/findBugs/findbugsTestCases/src clean install
 

Run selected tests
mvn -Prun-its -Dinvoker.test=build-*,basic-1,check clean install


Run tests in debugger
mvn -Dmaven.surefire.debug="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -Xnoagent -Djava.compiler=NONE" -Prun-its clean install 

