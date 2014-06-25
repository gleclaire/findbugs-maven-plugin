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

import org.apache.maven.artifact.repository.ArtifactRepository
import org.apache.maven.artifact.resolver.ArtifactResolver
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.doxia.tools.SiteTool
import org.apache.maven.project.MavenProject
import org.codehaus.gmaven.mojo.GroovyMojo
import org.codehaus.plexus.resource.ResourceManager
import org.codehaus.plexus.util.FileUtils

/**
 * Fail the build if there were any FindBugs violations in the source code.
 * An XML report is put out by default in the target directory with the errors.
 * To see more documentation about FindBugs' options, please see the <a href="http://findbugs.sourceforge.net/manual/index.html" class="externalLink">FindBugs
Manual.</a>.
 *
 * @since 2.0
 * @goal check
 * @phase verify
 * @execute goal="findbugs"
 * @requiresDependencyResolution compile
 * @requiresProject
 * @threadSafe
 *
 * @author <a href="mailto:gleclaire@codehaus.org">Garvin LeClaire</a>
 * @version $Id: FindbugsViolationCheckMojo.groovy gleclaire $
 */

class FindbugsViolationCheckMojo extends GroovyMojo {

	/**
	 * Location where generated html will be created.
	 *
	 * @parameter default-value="${project.reporting.outputDirectory}"
	 * @required
	 */

	File outputDirectory

	/**
	 * Turn on and off xml output of the Findbugs report.
	 *
	 * @parameter expression="${findbugs.xmlOutput}" default-value="false"
	 * @since 1.0.0
	 */
	boolean xmlOutput

	/**
	 * Specifies the directory where the xml output will be generated.
	 *
	 * @parameter default-value="${project.build.directory}"
	 * @required
	 * @since 1.0.0
	 */
	File xmlOutputDirectory

	/**
	 * This has been deprecated and is on by default.
	 *
	 * @parameter default-value="true"
	 * @since 1.2.0
	 * @deprecated
	 */
	boolean findbugsXmlOutput

	/**
	 * Specifies the directory where the findbugs native xml output will be generated.
	 *
	 * @parameter default-value="${project.build.directory}"
	 * @required
	 * @since 1.2.0
	 */
	File findbugsXmlOutputDirectory

	/**
	 * Doxia Site Renderer.
	 *
	 * @component
	 */
	Renderer siteRenderer

	/**
	 * Directory containing the class files for FindBugs to analyze.
	 *
	 * @parameter default-value="${project.build.outputDirectory}"
	 * @required
	 */
	File classFilesDirectory

	/**
	 * Directory containing the test class files for FindBugs to analyze.
	 *
	 * @parameter default-value="${project.build.testOutputDirectory}"
	 * @required
	 */
	File testClassFilesDirectory

	/**
	 * Location of the Xrefs to link to.
	 *
	 * @parameter default-value="${project.reporting.outputDirectory}/xref"
	 */
	File xrefLocation

	/**
	 * Location of the Test Xrefs to link to.
	 *
	 * @parameter default-value="${project.reporting.outputDirectory}/xref-test"
	 */
	File xrefTestLocation

	/**
	 * The directories containing the sources to be compiled.
	 *
	 * @parameter expression="${project.compileSourceRoots}"
	 * @required
	 * @readonly
	 */
	List compileSourceRoots

	/**
	 * The directories containing the test-sources to be compiled.
	 *
	 * @parameter expression="${project.testCompileSourceRoots}"
	 * @required
	 * @readonly
	 * @since 2.0
	 */
	List testSourceRoots

	/**
	 * Run Findbugs on the tests.
	 *
	 * @parameter expression="${findbugs.includeTests}" default-value="false"
	 * @since 2.0
	 */
	boolean includeTests

	/**
	 * List of artifacts this plugin depends on. Used for resolving the Findbugs coreplugin.
	 *
	 * @parameter expression="${plugin.artifacts}"
	 * @required
	 * @readonly
	 */
	ArrayList pluginArtifacts

	/**
	 * The local repository, needed to download the coreplugin jar.
	 *
	 * @parameter expression="${localRepository}"
	 * @required
	 * @readonly
	 */
	ArtifactRepository localRepository

	/**
	 * Remote repositories which will be searched for the coreplugin jar.
	 *
	 * @parameter expression="${project.remoteArtifactRepositories}"
	 * @required
	 * @readonly
	 */
	List remoteArtifactRepositories

	/**
	 * Maven Project
	 *
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	MavenProject project

	/**
	 * Encoding used for xml files. Default value is UTF-8.
	 *
	 * @parameter default-value="UTF-8"
	 * @readonly
	 */
	String xmlEncoding

	/**
	 * The file encoding to use when reading the source files. If the property <code>project.build.sourceEncoding</code>
	 * is not set, the platform default encoding is used.
	 *
	 * @parameter expression="${encoding}" default-value="${project.build.sourceEncoding}"
	 * @since 2.2
	 */
	String sourceEncoding

	/**
	 * The file encoding to use when creating the HTML reports. If the property <code>project.reporting.outputEncoding</code>
	 * is not set, the platform default encoding is used.
	 *
	 * @parameter expression="${outputEncoding}" default-value="${project.reporting.outputEncoding}"
	 * @since 2.2
	 */
	String outputEncoding

	/**
	 * Threshold of minimum bug severity to report. Valid values are High, Default, Low, Ignore, and Exp (for experimental).
	 *
	 * @parameter default-value="Default"
	 */
	String threshold

	/**
	 * Artifact resolver, needed to download the coreplugin jar.
	 *
	 * @component role="org.apache.maven.artifact.resolver.ArtifactResolver"
	 * @required
	 * @readonly
	 */
	ArtifactResolver artifactResolver

	/**
	 * <p>
	 * File name of the include filter. Only bugs in matching the filters are reported.
	 * </p>
	 *
	 * <p>
	 * Potential values are a filesystem path, a URL, or a classpath resource.
	 * </p>
	 *
	 * <p>
	 * This parameter is resolved as resource, URL, then file. If successfully
	 * resolved, the contents of the configuration is copied into the
	 * <code>${project.build.directory}</code>
	 * directory before being passed to Findbugs as a filter file.
	 * </p>
	 *
	 * @parameter
	 * @since 1.0-beta-1
	 */
	String includeFilterFile

	/**
	 * <p>
	 * File name of the exclude filter. Bugs matching the filters are not reported.
	 * </p>
	 *
	 * <p>
	 * Potential values are a filesystem path, a URL, or a classpath resource.
	 * </p>
	 *
	 * <p>
	 * This parameter is resolved as resource, URL, then file. If successfully
	 * resolved, the contents of the configuration is copied into the
	 * <code>${project.build.directory}</code>
	 * directory before being passed to Findbugs as a filter file.
	 * </p>
	 *
	 * @parameter
	 * @since 1.0-beta-1
	 */
	String excludeFilterFile

	/**
	 * <p>
	 * File names of the baseline files. Bugs found in the baseline files won't be reported.
	 * </p>
	 *
	 * <p>
	 * Potential values are a filesystem path, a URL, or a classpath resource.
	 * </p>
	 *
	 * <p>
	 * This parameter is resolved as resource, URL, then file. If successfully
	 * resolved, the contents of the configuration is copied into the
	 * <code>${project.build.directory}</code>
	 * directory before being passed to Findbugs as a filter file.
	 * </p>
	 *
	 * This is a comma-delimited list.
	 *
	 * @parameter
	 * @since 2.4.1
	 */
	String excludeBugsFile

	/**
	 * Effort of the bug finders. Valid values are Min, Default and Max.
	 *
	 * @parameter default-value="Default"
	 * @since 1.0-beta-1
	 */
	String effort

	/**
	 * turn on Findbugs debugging
	 *
	 * @parameter expression="${findbugs.debug}" default-value="false"
	 */
	Boolean debug

	/**
	 * Relaxed reporting mode. For many detectors, this option suppresses the heuristics used to avoid reporting false
	 * positives.
	 *
	 * @parameter expression="${findbugs.relaxed}" default-value="false"
	 * @since 1.1
	 */
	Boolean relaxed

	/**
	 * The visitor list to run. This is a comma-delimited list.
	 *
	 * @parameter
	 * @since 1.0-beta-1
	 */
	String visitors

	/**
	 * The visitor list to omit. This is a comma-delimited list.
	 *
	 * @parameter
	 * @since 1.0-beta-1
	 */
	String omitVisitors

	/**
	 * <p>
	 * The plugin list to include in the report. This is a comma-delimited list.
	 * </p>
	 *
	 * <p>
	 * Potential values are a filesystem path, a URL, or a classpath resource.
	 * </p>
	 *
	 * <p>
	 * This parameter is resolved as resource, URL, then file. If successfully
	 * resolved, the contents of the configuration is copied into the
	 * <code>${project.build.directory}</code>
	 * directory before being passed to Findbugs as a plugin file.
	 * </p>
	 *
	 * @parameter
	 * @since 1.0-beta-1
	 */
	String pluginList

	/**
	 * Restrict analysis to the given comma-separated list of classes and packages.
	 *
	 * @parameter
	 * @since 1.1
	 */
	String onlyAnalyze

	/**
	 * This option enables or disables scanning of nested jar and zip files found
	 *  in the list of files and directories to be analyzed.
	 *
	 * @parameter expression="${findbugs.nested}" default-value="false"
	 * @since 2.3.2
	 */
	Boolean nested

	/**
	 * Prints a trace of detectors run and classes analyzed to standard output.
	 * Useful for troubleshooting unexpected analysis failures.
	 *
	 * @parameter expression="${findbugs.trace}" default-value="false"
	 * @since 2.3.2
	 */
	Boolean trace

    /**
     * Maximum bug ranking to record.
     *
     * @parameter expression="${findbugs.maxRank}"
     * @since 2.5.5
     */
    int maxRank

    /**
	 * Skip entire check.
	 *
	 * @parameter expression="${findbugs.skip}" default-value="false"
	 * @since 1.1
	 */
	boolean skip

	/**
	 * @component
	 * @required
	 * @readonly
	 * @since 2.0
	 */
	ResourceManager resourceManager

	/**
	 * SiteTool.
	 *
	 * @since 2.1-SNAPSHOT
	 * @component role="org.apache.maven.doxia.tools.SiteTool"
	 * @required
	 * @readonly
	 */
	protected SiteTool siteTool

	/**
	 * Fail the build on an error.
	 *
	 * @parameter expression="${findbugs.failOnError}" default-value="true"
	 * @since 2.0
	 */
	boolean failOnError

	/**
	 * Fork a VM for FindBugs analysis.  This will allow you to set timeouts and heap size
	 *
	 * @parameter expression="${findbugs.fork}" default-value="true"
	 * @since 2.3.2
	 */
	boolean fork

	/**
	 * Maximum Java heap size in megabytes  (default=512).
	 * This only works if the <b>fork</b> parameter is set <b>true</b>.
	 *
	 * @parameter default-value="512"
	 * @since 2.2
	 */
	int maxHeap

	/**
	 * Specifies the amount of time, in milliseconds, that FindBugs may run before
	 *  it is assumed to be hung and is terminated.
	 * The default is 600,000 milliseconds, which is ten minutes.
	 * This only works if the <b>fork</b> parameter is set <b>true</b>.
	 *
	 * @parameter default-value="600000"
	 * @since 2.2
	 */
	int timeout

	/**
	* <p>
	* the arguments to pass to the forked VM (ignored if fork is disabled).
	* </p>
	*
	* @parameter
	* @since 2.4.1
	*/
   String jvmArgs


	int bugCount

	int errorCount


	void execute() {
		Locale locale = Locale.getDefault()
		List sourceFiles

		log.debug("Excecuting findbugs:check")

		if ( this.classFilesDirectory.exists() && this.classFilesDirectory.isDirectory() ) {
			sourceFiles = FileUtils.getFiles(classFilesDirectory, FindBugsInfo.JAVA_REGEX_PATTERN, null)
		}

		if ( !skip && sourceFiles ) {

			// this goes

			log.debug("Here goes...............Excecuting findbugs:check")

			if (!findbugsXmlOutputDirectory.exists()) {
				if ( !findbugsXmlOutputDirectory.mkdirs() ) {
					fail("Cannot create xml output directory")
				}
			}


			File outputFile = new File("${findbugsXmlOutputDirectory}/findbugsXml.xml")

			if (outputFile.exists()) {

				def path = new XmlSlurper().parse(outputFile)

				def allNodes = path.depthFirst().collect { it }

				bugCount = allNodes.findAll {it.name() == 'BugInstance'}.size()
				log.info("BugInstance size is ${bugCount}")

				errorCount = allNodes.findAll {it.name() == 'Error'}.size()
				log.info("Error size is ${errorCount}")

        def xml = new XmlParser().parse(outputFile)
        def bugs = xml.BugInstance
        def total = bugs.size()
        
        if (total <= 0) {
          log.info('No errors/warnings found')
          return
        }
        
        log.info('Total bugs: ' + total)
        for (i in 0..total-1) {
          def bug = bugs[i]
          log.info( bug.LongMessage.text() + FindBugsInfo.BLANK + bug.Class.'@classname' + FindBugsInfo.BLANK + bug.Class.SourceLine.Message.text() )
        }


				if ( (bugCount || errorCount) && failOnError ) {
					fail("failed with ${bugCount} bugs and ${errorCount} errors ")
				}
			}
		}
		else {
			log.debug("Nothing for FindBugs to do here.")
		}
	}

}
