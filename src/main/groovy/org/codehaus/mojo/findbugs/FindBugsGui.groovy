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

import org.apache.maven.project.MavenProject
import org.codehaus.gmaven.mojo.GroovyMojo

/**
 * Launch the Findbugs GUI.
 * It will use all the parameters in the POM fle.
 *
 * @since 2.0
 * @goal gui
 *
 *
 * @description Launch the Findbugs GUI using the parameters in the POM fle.
 * @requiresDependencyResolution compile
 * @requiresProject
 *
 * @author <a href="mailto:gleclaire@codehaus.org">Garvin LeClaire</a>
 * @version $Id: FindBugsGui.groovy gleclaire $
 */

class FindBugsGui extends GroovyMojo {


    /**
     * locale to use for Resource bundle.
     *
     *
     */
    static Locale locale = Locale.ENGLISH

    /**
     * Directory containing the class files for FindBugs to analyze.
     *
     * @parameter default-value="${project.build.outputDirectory}"
     * @required
     */
    File classFilesDirectory

    /**
     * turn on Findbugs debugging
     *
     * @parameter default-value="false"
     */
    Boolean debug

    /**
     * List of artifacts this plugin depends on. Used for resolving the Findbugs coreplugin.
     *
     * @parameter expression="${plugin.artifacts}"
     * @required
     * @readonly
     */
    ArrayList pluginArtifacts

    /**
     * Effort of the bug finders. Valid values are Min, Default and Max.
     *
     * @parameter default-value="Default"
     *
     */
    String effort


    /**
     * The plugin list to include in the report. This is a FindBugsInfo.COMMA-delimited list.
     *
     * @parameter
     *
     */
    String pluginList

    /**
     * Maven Project
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    MavenProject project

    /**
     * Resource bundle for a specific locale.
     *
     * @parameter
     * @readonly
     *
     */
    ResourceBundle bundle

    /**
     * Specifies the directory where the findbugs native xml output will be generated.
     *
     * @parameter default-value="${project.build.directory}"
     * @required
     *
     */
    File findbugsXmlOutputDirectory

    /**
     * The file encoding to use when reading the source files. If the property <code>project.build.sourceEncoding</code>
     * is not set, the platform default encoding is used. <strong>Note:</strong> This parameter always overrides the
     * property <code>charset</code> from Checkstyle's <code>TreeWalker</code> module.
     *
     * @parameter expression="${encoding}" default-value="${project.build.sourceEncoding}"
     * @since 2.2
     */
    String encoding

    /**
     * Maximum Java heap size in megabytes  (default=512).
     *
     * @parameter default-value="512"
     * @since 2.2
     */
    int maxHeap

    void execute() {

        def auxClasspathElements = project.compileClasspathElements

        if ( debug ) {
            log.debug("  Plugin Artifacts to be added ->" + pluginArtifacts.toString())
        }




        ant.project.setProperty('basedir', findbugsXmlOutputDirectory.getAbsolutePath())
        ant.project.setProperty('verbose', "true")


        ant.java(classname: "edu.umd.cs.findbugs.LaunchAppropriateUI", fork: "true", failonerror: "true", clonevm: "true", maxmemory: "${maxHeap}m")
        {

            def effectiveEncoding = System.getProperty( "file.encoding", "UTF-8" )

            if ( encoding ) { effectiveEncoding = encoding }

            log.info("File Encoding is " + effectiveEncoding)

            sysproperty(key: "file.encoding" , value: effectiveEncoding)
            def findbugsXmlName = findbugsXmlOutputDirectory.toString() + "/findbugsXml.xml"
            def findbugsXml = new File(findbugsXmlName)

            if ( findbugsXml.exists() ) {
                log.debug("  Found an FindBugs XML at ->" + findbugsXml.toString())
                arg(value: findbugsXml)
            }


            classpath()
            {

                pluginArtifacts.each() {pluginArtifact ->
                    if ( debug ) {
                        log.debug("  Trying to Add to pluginArtifact ->" + pluginArtifact.file.toString())
                    }

                    pathelement(location: pluginArtifact.file)
                }
            }
        }
    }
}