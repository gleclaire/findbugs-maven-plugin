# !/bin/bash
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------
#   Copyright (c) 2001-2002 The Apache Software Foundation.  All rights
#   reserved.
# ----------------------------------------------------------------------------

function sendToRepo()
{
  
  REPO="https://oss.sonatype.org/service/local/staging/deploy/maven2/"
  
  mvn org.apache.maven.plugins:maven-gpg-plugin:1.5:sign-and-deploy-file -Durl=$REPO -DrepositoryId=sonatype-nexus-staging -DpomFile=$POM_FILE -Dfile=$JAR_FILE
  mvn org.apache.maven.plugins:maven-gpg-plugin:1.5:sign-and-deploy-file -Durl=$REPO -DrepositoryId=sonatype-nexus-staging -DpomFile=$POM_FILE -Dfile=src.jar -Dclassifier=sources
  mvn org.apache.maven.plugins:maven-gpg-plugin:1.5:sign-and-deploy-file -Durl=$REPO -DrepositoryId=sonatype-nexus-staging -DpomFile=$POM_FILE -Dfile=apidocs.jar -Dclassifier=javadoc
}

echo ""
echo usage: fb2repo version
echo ""
echo ""
echo $"FINDBUGS_HOME is $FINDBUGS_HOME : "
echo ""

POM_FILE="bcel.pom"
JAR_FILE="$FINDBUGS_HOME/lib/bcel-6.0-SNAPSHOT.jar"
sendToRepo

# POM_FILE="jsr305.pom"
# JAR_FILE="$FINDBUGS_HOME/lib/jsr305.jar"
# sendToRepo

POM_FILE="annotations.pom"
JAR_FILE="$FINDBUGS_HOME/lib/annotations.jar"
sendToRepo

POM_FILE="jFormatString.pom"
JAR_FILE="$FINDBUGS_HOME/lib/jFormatString.jar"
sendToRepo

POM_FILE="findbugs.pom"
JAR_FILE="$FINDBUGS_HOME/lib/findbugs.jar"
sendToRepo

POM_FILE="findbugs-ant.pom"
JAR_FILE="$FINDBUGS_HOME/lib/findbugs-ant.jar"
sendToRepo

