#!/bin/bash
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

function sendToLocalRepo()
{
  
  mvn org.apache.maven.plugins:maven-install-plugin:2.5.1:install-file -DpomFile=$POM_FILE -Dfile=$JAR_FILE -DgroupId=com.google.code.findbugs -DartifactId=$ARTIFACT_ID -Dversion=$DEPLOY_VERSION -Dpackaging=jar -Dsources=src.jar -Djavadoc=apidocs.jar -DcreateChecksum=true

}


echo ""
echo "usage: fb2local version"
echo ""
echo $"FINDBUGS_HOME is $FINDBUGS_HOME : "
echo ""

POM_FILE="bcel.pom"
JAR_FILE="$FINDBUGS_HOME/lib/bcel-6.0-SNAPSHOT.jar"
DEPLOY_VERSION="6.0"
ARTIFACT_ID="bcel-findbugs"
sendToLocalRepo

POM_FILE="jsr305.pom"
JAR_FILE="$FINDBUGS_HOME/lib/jsr305.jar"
DEPLOY_VERSION="$1"
ARTIFACT_ID="jsr305"
sendToLocalRepo

POM_FILE="annotations.pom"
JAR_FILE="$FINDBUGS_HOME/lib/annotations.jar"
DEPLOY_VERSION="$1"
ARTIFACT_ID="annotations"
sendToLocalRepo

POM_FILE="jFormatString.pom"
JAR_FILE="$FINDBUGS_HOME/lib/jFormatString.jar"
DEPLOY_VERSION="$1"
ARTIFACT_ID="jFormatString"
sendToLocalRepo

POM_FILE="findbugs.pom"
JAR_FILE="$FINDBUGS_HOME/lib/findbugs.jar"
DEPLOY_VERSION="$1"
ARTIFACT_ID="findbugs"
sendToLocalRepo

POM_FILE="findbugs-ant.pom"
JAR_FILE="$FINDBUGS_HOME/lib/findbugs-ant.jar"
DEPLOY_VERSION="$1"
ARTIFACT_ID="findbugs-ant"
sendToLocalRepo

