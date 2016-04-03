package org.codehaus.mojo.findbugs

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.artifact.Artifact
import org.apache.maven.plugin.MojoExecutionException


/**
 * FindBugs plugin support for Mojos.
 */

trait FindBugsPluginsTrait {

    // properties in traits should be supported but don't compile currently:
    // https://issues.apache.org/jira/browse/GROOVY-7536
    // when fixed, should move pluginList and plugins properties here

    /**
     * Adds the specified plugins to findbugs. The coreplugin is always added first.
     *
     */
    // the method uses fields from implementing classes directly, is that wrong? maybe should receive them as parameters
    String getFindbugsPlugins() {
        ResourceHelper resourceHelper = new ResourceHelper(log)

        URL[] pluginURL

        def urlPlugins = ""

        if (pluginList) {
            log.debug("  Adding Plugins ")
            String[] pluginJars = pluginList.split(FindBugsInfo.COMMA)

            pluginJars.each() { pluginJar ->
                def pluginFileName = pluginJar.trim()

                if (!pluginFileName.endsWith(".jar")) {
                    throw new IllegalArgumentException("Plugin File is not a Jar file: " + pluginFileName)
                }

                try {
                    log.debug("  Processing Plugin: " + pluginFileName.toString())

                    urlPlugins += resourceHelper.getResourceFile(pluginFileName.toString(), findbugsXmlOutputDirectory).absolutePath + ((pluginJar == pluginJars[pluginJars.size() - 1]) ? "" : File.pathSeparator)
                } catch (MalformedURLException exception) {
                    throw new MojoExecutionException("The addin plugin has an invalid URL")
                }
            }
        }

        if (plugins) {
            log.debug("  Adding Plugins from a repository")

            if (urlPlugins.size() > 0) {
                urlPlugins += File.pathSeparator
            }

            Artifact pomArtifact

            plugins.each() { plugin ->

                log.debug("  Processing Plugin: " + plugin.toString())
                log.debug("groupId is ${plugin['groupId']} ****** artifactId is ${plugin['artifactId']} ****** version is ${plugin['version']} ****** type is ${plugin['type']}")
                pomArtifact = this.factory.createArtifact(plugin['groupId'], plugin['artifactId'], plugin['version'], "", plugin['type'])
                log.debug("pomArtifact is ${pomArtifact} ****** groupId is ${plugin['groupId']} ****** artifactId is ${plugin['artifactId']} ****** version is ${plugin['version']} ****** type is ${plugin['type']}")

                artifactResolver.resolve(pomArtifact, this.remoteRepositories, this.localRepository)

                urlPlugins += resourceHelper.getResourceFile(pomArtifact.file.absolutePath, findbugsXmlOutputDirectory).absolutePath + ((plugin == plugins[plugins.size() - 1]) ? "" : File.pathSeparator)
            }
        }


        log.debug("  Plugin list is: ${urlPlugins}")

        return urlPlugins
    }

}
