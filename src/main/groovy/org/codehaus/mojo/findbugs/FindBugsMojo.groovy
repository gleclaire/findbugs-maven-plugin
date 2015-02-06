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
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository
import org.apache.maven.artifact.resolver.ArtifactResolver
import org.apache.maven.doxia.siterenderer.Renderer
import org.apache.maven.doxia.tools.SiteTool
import org.apache.maven.project.MavenProject
import org.apache.maven.reporting.AbstractMavenReport
import org.codehaus.plexus.resource.ResourceManager
import org.codehaus.plexus.resource.loader.FileResourceCreationException
import org.codehaus.plexus.resource.loader.FileResourceLoader

import groovy.xml.StreamingMarkupBuilder
import org.codehaus.plexus.util.FileUtils

import org.sonatype.plexus.build.incremental.BuildContext


/**
 * Generates a FindBugs Report when the site plugin is run.
 * The HTML report is generated for site commands only.
 *
 * @goal findbugs
 * @phase compile
 * @requiresDependencyResolution compile
 * @requiresProject
 * @threadSafe
 *
 * @author <a href="mailto:gleclaire@codehaus.org">Garvin LeClaire</a>
 * @version $Id: FindBugsMojo.groovy 16932 2012-06-21 01:13:14Z gleclaire $
 */

class FindBugsMojo extends AbstractMavenReport {

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
     * @parameter property="findbugs.xmlOutput" default-value="false"
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
     *
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
     * @parameter property="project.compileSourceRoots"
     * @required
     * @readonly
     */
    List compileSourceRoots

    /**
     * The directories containing the test-sources to be compiled.
     *
     * @parameter property="project.testCompileSourceRoots"
     * @required
     * @readonly
     * @since 2.0
     */
    List testSourceRoots

    /**
     * Run Findbugs on the tests.
     *
     * @parameter property="findbugs.includeTests" default-value="false"
     * @since 2.0
     */
    boolean includeTests

    /**
     * List of artifacts this plugin depends on. Used for resolving the Findbugs coreplugin.
     *
     * @parameter property="plugin.artifacts"
     * @required
     * @readonly
     */
    ArrayList pluginArtifacts

    /**
     * List of Remote Repositories used by the resolver
     *
     * @parameter property="project.remoteArtifactRepositories"
     * @readonly
     * @required
     */
    List remoteRepositories

    /**
     * The local repository, needed to download the coreplugin jar.
     *
     * @parameter property="localRepository"
     * @required
     * @readonly
     */
    ArtifactRepository localRepository

    /**
     * Remote repositories which will be searched for the coreplugin jar.
     *
     * @parameter property="project.remoteArtifactRepositories"
     * @required
     * @readonly
     */
    List remoteArtifactRepositories

    /**
     * Maven Project
     *
     * @parameter property="project"
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
     * @parameter property="encoding" default-value="${project.build.sourceEncoding}"
     * @since 2.2
     */
    String sourceEncoding

    /**
     * The file encoding to use when creating the HTML reports. If the property <code>project.reporting.outputEncoding</code>
     * is not set, the platform default encoding is used.
     *
     * @parameter property="outputEncoding" default-value="${project.reporting.outputEncoding}"
     * @since 2.2
     */
    String outputEncoding

    /**
     * Threshold of minimum bug severity to report. Valid values are High, Default, Low, Ignore, and Exp (for experimental).
     *
     * @parameter property="findbugs.threshold" default-value="Default"
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
     * Used to look up Artifacts in the remote repository.
     *
     * @parameter property="component.org.apache.maven.artifact.factory.ArtifactFactory"
     * @required
     * @readonly
     */
    ArtifactFactory factory

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
     * @parameter property="findbugs.includeFilterFile"
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
     * @parameter property="findbugs.excludeFilterFile"
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
     * @parameter property="findbugs.excludeBugsFile"
     * @since 2.4.1
     */
    String excludeBugsFile

    /**
     * Effort of the bug finders. Valid values are Min, Default and Max.
     *
     * @parameter property="findbugs.effort" default-value="Default"
     * @since 1.0-beta-1
     */
    String effort

    /**
     * turn on Findbugs debugging
     *
     * @parameter property="findbugs.debug" default-value="false"
     */
    Boolean debug

    /**
     * Relaxed reporting mode. For many detectors, this option suppresses the heuristics used to avoid reporting false
     * positives.
     *
     * @parameter property="findbugs.relaxed" default-value="false"
     * @since 1.1
     */
    Boolean relaxed

    /**
     * The visitor list to run. This is a comma-delimited list.
     *
     * @parameter property="findbugs.visitors"
     * @since 1.0-beta-1
     */
    String visitors

    /**
     * The visitor list to omit. This is a comma-delimited list.
     *
     * @parameter property="findbugs.omitVisitors"
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
     * @parameter property="findbugs.pluginList"
     * @since 1.0-beta-1
     */
    String pluginList

    /**
     * <p>
     * Collection of PluginArtifact to work on. (PluginArtifact contains groupId, artifactId, version, type.)
     * See <a href="./usage.html#Using Detectors from a Repository">Usage</a> for details.
     * </p>
     *
     *
     * @parameter
     * @since 2.4.1
     */
    PluginArtifact[] plugins;

    /**
     * Restrict analysis to the given comma-separated list of classes and packages.
     *
     * @parameter property="findbugs.onlyAnalyze"
     * @since 1.1
     */
    String onlyAnalyze

    /**
     * This option enables or disables scanning of nested jar and zip files found
     *  in the list of files and directories to be analyzed.
     *
     * @parameter property="findbugs.nested" default-value="false"
     * @since 2.3.2
     */
    Boolean nested

    /**
     * Prints a trace of detectors run and classes analyzed to standard output.
     * Useful for troubleshooting unexpected analysis failures.
     *
     * @parameter property="findbugs.trace" default-value="false"
     * @since 2.3.2
     */
    Boolean trace

    /**
     * Maximum bug ranking to record.
     *
     * @parameter property="findbugs.maxRank"
     * @since 2.4.1
     */
    int maxRank

    /**
     * Skip entire check.
     *
     * @parameter property="findbugs.skip" default-value="false"
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
     * @parameter property="findbugs.failOnError" default-value="true"
     * @since 2.0
     */
    boolean failOnError

    /**
     * Fork a VM for FindBugs analysis.  This will allow you to set timeouts and heap size
     *
     * @parameter property="findbugs.fork" default-value="true"
     * @since 2.3.2
     */
    boolean fork

    /**
     * Maximum Java heap size in megabytes  (default=512).
     * This only works if the <b>fork</b> parameter is set <b>true</b>.
     *
     * @parameter property="findbugs.maxHeap" default-value="512"
     * @since 2.2
     */
    int maxHeap

    /**
     * Specifies the amount of time, in milliseconds, that FindBugs may run before
     *  it is assumed to be hung and is terminated.
     * The default is 600,000 milliseconds, which is ten minutes.
     * This only works if the <b>fork</b> parameter is set <b>true</b>.
     *
     * @parameter property="findbugs.timeout" default-value="600000"
     * @since 2.2
     */
    int timeout

    /**
     * <p>
     * the arguments to pass to the forked VM (ignored if fork is disabled).
     * </p>
     *
     * @parameter property="findbugs.jvmArgs"
     * @since 2.4.1
     */
    String jvmArgs


    int bugCount
    int errorCount

    ResourceBundle bundle

    /**
     * Checks whether prerequisites for generating this report are given.
     *
     * @return true if report can be generated, otherwise false
     * @see org.apache.maven.reporting.MavenReport#canGenerateReport()
     */
    boolean canGenerateReport() {

        def canGenerate = false
        log.debug("Inside canGenerateReport..... ${canGenerate} ")

        if (!skip && classFilesDirectory.exists()) {

            classFilesDirectory.eachFileRecurse {
                if (it.name.contains(FindBugsInfo.CLASS_SUFFIX)) {
                    canGenerate = true
                }
            }
            log.debug("canGenerate Src is ${canGenerate}")
        }

        if (!skip && testClassFilesDirectory.exists() && includeTests) {

            testClassFilesDirectory.eachFileRecurse {
                if (it.name.contains(FindBugsInfo.CLASS_SUFFIX)) {
                    canGenerate = true
                }
            }
            log.debug("canGenerate Test Src is ${canGenerate}")
        }


        log.debug("canGenerate is ${canGenerate}")

        return canGenerate
    }

    /**
     * Returns the plugins description for the "generated reports" overview page.
     *
     * @param locale
     *            the locale the report should be generated for
     *
     * @return description of the report
     * @see org.apache.maven.reporting.MavenReport#getDescription(java.util.Locale)
     */
    String getDescription(Locale locale) {
        return getBundle(locale).getString(FindBugsInfo.DESCRIPTION_KEY)
    }

    /**
     * Returns the plugins name for the "generated reports" overview page and the menu.
     *
     * @param locale
     *            the locale the report should be generated for
     *
     * @return name of the report
     * @see org.apache.maven.reporting.MavenReport#getName(java.util.Locale)
     */
    String getName(Locale locale) {
        return getBundle(locale).getString(FindBugsInfo.NAME_KEY)
    }

    /**
     * Returns report output file name, without the extension.
     *
     * Called by AbstractMavenReport.execute() for creating the sink.
     *
     * @return name of the generated page
     * @see org.apache.maven.reporting.MavenReport#getOutputName()
     */
    String getOutputName() {
        return FindBugsInfo.PLUGIN_NAME
    }

    /**
     * Executes the generation of the report.
     *
     * Callback from Maven Site Plugin.
     *
     * @param locale he wanted locale to generate the report, could be null.
     *
     * @see org.apache.maven.reporting.MavenReport #executeReport(java.util.Locale)
     */
    void executeReport(Locale locale) {

        if (canGenerateReport()) {

            log.info("Locale is ${locale.getLanguage()}")

            log.debug("****** FindBugsMojo executeReport *******")


            resourceManager.addSearchPath(FileResourceLoader.ID, this.project.getFile().getParentFile().getAbsolutePath())
            resourceManager.addSearchPath(FindBugsInfo.URL, "")

            resourceManager.setOutputDirectory(new File(this.project.getBuild().getDirectory()))


            log.debug("report Output Directory is " + getReportOutputDirectory())
            log.debug("Output Directory is " + outputDirectory)
            log.debug("Classes Directory is " + classFilesDirectory)

            log.debug("resourceManager outputDirectory is " + resourceManager.outputDirectory)


            log.debug("  Plugin Artifacts to be added ->" + pluginArtifacts.toString())

            if (!findbugsXmlOutputDirectory.exists()) {
                if (!findbugsXmlOutputDirectory.mkdirs()) {
                    fail("Cannot create xml output directory")
                }
            }

            File outputFile = new File("${findbugsXmlOutputDirectory}/findbugsXml.xml")

            log.debug("XML outputFile is " + outputFile.getAbsolutePath())
            log.debug("XML output Directory is " + findbugsXmlOutputDirectory.getAbsolutePath())

            ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            try {
                // The SAX parser factory will fail with CCE if the TCCL is out of sync with our class loader
                // This for Maven 2.2.1 only MFINDBUGS-178
                Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
                executeFindbugs(locale, outputFile)
            } finally {
                Thread.currentThread().setContextClassLoader(tccl);
            }

            if (!outputDirectory.exists()) {
                if (!outputDirectory.mkdirs()) {
                    fail("Cannot create html output directory")
                }
            }

            if (outputFile.exists()) {
                log.debug("Generating Findbugs HTML")

                FindbugsReportGenerator generator = new FindbugsReportGenerator(getSink(), getBundle(locale), this.project.getBasedir(), siteTool)

                boolean isJxrPluginEnabled = isJxrPluginEnabled()

                generator.setIsJXRReportEnabled(isJxrPluginEnabled)

                if (isJxrPluginEnabled) {
                    generator.setCompileSourceRoots(this.compileSourceRoots)
                    generator.setTestSourceRoots(this.testSourceRoots)
                    generator.setXrefLocation(this.xrefLocation)
                    generator.setXrefTestLocation(this.xrefTestLocation)
                    generator.setIncludeTests(this.includeTests)
                }


                generator.setLog(log)

                generator.threshold = threshold

                generator.effort = effort

                generator.setFindbugsResults(new XmlSlurper().parse(outputFile))


                generator.setOutputDirectory(new File(outputDirectory.getAbsolutePath()))

                generator.generateReport()


                log.debug("xmlOutput is ${xmlOutput}")


                if (xmlOutput) {
                    log.debug("  Using the xdoc format")

                    if (!xmlOutputDirectory.exists()) {
                        if (!xmlOutputDirectory.mkdirs()) {
                            fail("Cannot create xdoc output directory")
                        }
                    }

                    XDocsReporter xDocsReporter = new XDocsReporter(getBundle(locale), log, threshold, effort, outputEncoding)
                    xDocsReporter.setOutputWriter(new OutputStreamWriter(new FileOutputStream(new File("${xmlOutputDirectory}/findbugs.xml")), outputEncoding))
                    xDocsReporter.setFindbugsResults(new XmlSlurper().parse(outputFile))
                    xDocsReporter.setCompileSourceRoots(this.compileSourceRoots)
                    xDocsReporter.setTestSourceRoots(this.testSourceRoots)

                    xDocsReporter.generateReport()
                }
            }
        } else {
            log.info("cannot generate report");
        }
    }

    /**
     * Returns the report output directory.
     *
     * Called by AbstractMavenReport.execute() for creating the sink.
     *
     * @return full path to the directory where the files in the site get copied to
     * @see org.apache.maven.reporting.AbstractMavenReport#getOutputDirectory()
     */
    protected String getOutputDirectory() {
        return outputDirectory.getAbsolutePath()
    }

    /**
     * Return the project.
     *
     * @return the project.
     * @see org.apache.maven.reporting.AbstractMavenReport#getProject()
     */
    protected MavenProject getProject() {
        return this.project
    }

    /**
     * Return the Sire Renderer.
     *
     */
    protected Renderer getSiteRenderer() {
        return this.siteRenderer
    }

    /**
     * Determines if the JXR-Plugin is included in the report section of the POM.
     *
     * @param bundle
     *            The bundle to load the artifactIf of the jxr plugin.
     * @return True if the JXR-Plugin is included in the POM, false otherwise.
     *
     */
    protected boolean isJxrPluginEnabled() {
        boolean isEnabled = false

        if (xrefLocation.exists()) {
            isEnabled = true
            return isEnabled
        }


        List reportPlugins = getProject().getReportPlugins()

        reportPlugins.each() { reportPlugin ->

            log.debug("report plugin -> ${reportPlugin.getArtifactId()}")
            if ("maven-jxr-plugin".equals(reportPlugin.getArtifactId()) || "jxr-maven-plugin".equals(reportPlugin.getArtifactId())) {
                isEnabled = true
            }
        }

        log.debug("jxr report links are ${isEnabled ? "enabled" : "disabled"}")
        return isEnabled
    }


    ResourceBundle getBundle(locale) {

        this.bundle = ResourceBundle.getBundle(FindBugsInfo.BUNDLE_NAME, locale, FindBugsMojo.class.getClassLoader())

        log.debug("Mojo Locale is " + this.bundle.getLocale().getLanguage())

        return bundle
    }

    public void execute() {
        log.debug("****** FindBugsMojo execute *******")
        File outputFile = new File("${findbugsXmlOutputDirectory}/findbugsXml.xml")

        log.debug("XML outputFile is " + outputFile.getAbsolutePath())

        log.debug("Generating Findbugs XML")
        if (canGenerateReport()) {
            executeFindbugs(new Locale("pt", "BR"), outputFile)
        }
    }

    /**
     * Get the Findbugs command line arguments.
     *
     * @param Findbugs temp output file
     *
     * @return Findbugs command line arguments.
     *
     */
    private ArrayList<String> getFindbugsArgs(File tempFile) {
        def args = new ArrayList<String>()

        args << "-xml:withMessages"

        args << "-auxclasspathFromInput"

        args << "-projectName"
        args << "${project.name}"

        args << getEffortParameter()
        args << getThresholdParameter()

        if (debug) {
            log.debug("progress on")
            args << "-progress"
        }

        if (pluginList || plugins) {
            args << "-pluginList"
            args << getFindbugsPlugins()
        }


        if (visitors) {
            args << "-visitors"
            args << visitors
        }

        if (omitVisitors) {
            args << "-omitVisitors"
            args << omitVisitors
        }

        if (relaxed) {
            args << "-relaxed"
        }

        if (nested) {
            args << "-nested:true"
        } else {
            args << "-nested:false"
        }

        if (onlyAnalyze) {
            args << "-onlyAnalyze"
            args << onlyAnalyze
        }


        if (includeFilterFile) {
            log.debug("  Adding Include Filter File ")

            args << "-include"
            args << getResourceFile(includeFilterFile.trim())
        }

        if (excludeFilterFile) {
            log.debug("  Adding Exclude Filter File ")

             args << "-exclude"
             args << getResourceFile(excludeFilterFile.trim())
        }

        if (excludeBugsFile) {
            log.debug("  Adding Exclude Bug Files (Baselines)")
            String[] excludeFiles = excludeBugsFile.split(FindBugsInfo.COMMA)

            excludeFiles.each() { excludeFile ->
                args << "-excludeBugs"
                args << getResourceFile(excludeFile.trim())
            }
        }

        if (maxRank) {
            args << "-maxRank"
            args << maxRank
        }

        args << "-output"
        args << tempFile.getAbsolutePath()


        if (classFilesDirectory.exists() && classFilesDirectory.isDirectory()) {
            log.debug("  Adding to Source Directory ->" + classFilesDirectory.absolutePath)
            args << classFilesDirectory.absolutePath
        }

        if (testClassFilesDirectory.exists() && testClassFilesDirectory.isDirectory() && includeTests) {
            log.debug("  Adding to Source Directory ->" + testClassFilesDirectory.absolutePath)
            args << testClassFilesDirectory.absolutePath
        }

        return args
    }

    /**
     * Get the Findbugs AuxClasspath.
     *
     */
    private String getFindbugsAuxClasspath() {
        def auxClasspathElements

        if (classFilesDirectory.exists() && classFilesDirectory.isDirectory()) {
            auxClasspathElements = project.compileClasspathElements
        }

        if (testClassFilesDirectory.exists() && testClassFilesDirectory.isDirectory() && includeTests) {
            auxClasspathElements = project.testClasspathElements
        }


        def auxClasspath = ""

        pluginArtifacts.each() { pluginArtifact ->
            log.debug("  Adding to AuxClasspath ->" + pluginArtifact.file.toString())

            auxClasspath += pluginArtifact.file.toString() + ((pluginArtifact == pluginArtifacts[pluginArtifacts.size() - 1]) ? "" : File.pathSeparator)
        }

        log.debug("  auxClasspathElements -> ${auxClasspathElements}")

        if (auxClasspathElements) {

            log.debug("  AuxClasspath Elements ->" + auxClasspathElements)


            def auxClasspathList = auxClasspathElements.findAll { project.build.outputDirectory != it.toString() }

            if (auxClasspathList.size() > 0) {

                auxClasspath += File.pathSeparator

                log.debug("  Last AuxClasspath is ->" + auxClasspathList[auxClasspathList.size() - 1])

                auxClasspathList.each() { auxClasspathElement ->

                    log.debug("  Adding to AuxClasspath ->" + auxClasspathElement.toString())

                    auxClasspath += auxClasspathElement.toString() + ((auxClasspathElement == auxClasspathList[auxClasspathList.size() - 1]) ? "" : File.pathSeparator)
                }
            }
        }

        log.debug("  AuxClasspath is ->" + auxClasspath)

        return auxClasspath
    }

    /**
     * Set up and run the Findbugs engine.
     *
     * @param locale
     *            the locale the report should be generated for
     *
     */
    public void executeFindbugs(Locale locale, File outputFile) {

        log.debug("****** FindBugsMojo executeFindbugs *******")
        long startTime, duration

        File tempFile = new File("${project.build.directory}/findbugsTemp.xml")

        if (tempFile.exists()) {
            tempFile.delete()
        }

        tempFile.getParentFile().mkdirs()
        tempFile.createNewFile()

        outputEncoding = outputEncoding ?: 'UTF-8'

        log.debug("****** Executing FindBugsMojo *******")

        resourceManager.addSearchPath(FileResourceLoader.ID, project.getFile().getParentFile().getAbsolutePath())
        resourceManager.addSearchPath(FindBugsInfo.URL, "")

        resourceManager.setOutputDirectory(new File(project.getBuild().getDirectory()))

        log.debug("resourceManager outputDirectory is " + resourceManager.outputDirectory)


        log.debug("  Plugin Artifacts to be added -> ${pluginArtifacts.toString()}")

        log.debug("outputFile is " + outputFile.getCanonicalPath())
        log.debug("output Directory is " + findbugsXmlOutputDirectory.getAbsolutePath())

        log.debug("Temp File is " + tempFile.getCanonicalPath())

        def ant = new AntBuilder()

        log.info("Fork Value is ${fork}")

        if (log.isDebugEnabled()) {
            startTime = System.nanoTime()
        }

        def findbugsArgs = getFindbugsArgs(tempFile)

        ant.java(classname: "edu.umd.cs.findbugs.FindBugs2", inputstring: getFindbugsAuxClasspath(), fork: "${fork}", failonerror: "true", clonevm: "false", timeout: "${timeout}", maxmemory: "${maxHeap}m") {

            def effectiveEncoding = System.getProperty("file.encoding", "UTF-8")

            if (sourceEncoding) {
                effectiveEncoding = sourceEncoding
            }

            log.debug("File Encoding is " + effectiveEncoding)

            sysproperty(key: "file.encoding", value: effectiveEncoding)

            if (jvmArgs && fork) {
                log.debug("Adding JVM Args => ${jvmArgs}")

                String[] args = jvmArgs.split(FindBugsInfo.BLANK)

                args.each() { jvmArg ->
                    log.debug("Adding JVM Arg => ${jvmArg}")
                    jvmarg(value: jvmArg)
                }
            }

            if (debug || trace) {
                sysproperty(key: "findbugs.debug", value: true)
            }

            classpath() {

                pluginArtifacts.each() { pluginArtifact ->
                    log.debug("  Adding to pluginArtifact ->" + pluginArtifact.file.toString())

                    pathelement(location: pluginArtifact.file)
                }
            }


            findbugsArgs.each { findbugsArg ->
                log.debug("Findbugs arg is ${findbugsArg}")
                arg(value: findbugsArg)
            }

        }



        if (log.isDebugEnabled()) {
            duration = (System.nanoTime() - startTime) / 1000000000.00
            log.debug("FindBugs duration is ${duration}")
        }

        log.info("Done FindBugs Analysis....")

        if (tempFile.exists()) {

            if (tempFile.size() > 0) {
                def path = new XmlSlurper().parse(tempFile)

                def allNodes = path.depthFirst().collect { it }

                bugCount = allNodes.findAll { it.name() == 'BugInstance' }.size()
                log.debug("BugInstance size is ${bugCount}")

                errorCount = allNodes.findAll { it.name() == 'Error' }.size()
                log.debug("Error size is ${errorCount}")



                def xmlProject = path.Project

                compileSourceRoots.each() { compileSourceRoot ->
                    xmlProject.appendNode { SrcDir(compileSourceRoot) }
                }

                if (testClassFilesDirectory.exists() && testClassFilesDirectory.isDirectory() && includeTests) {
                    testSourceRoots.each() { testSourceRoot ->
                        xmlProject.appendNode { SrcDir(testSourceRoot) }
                    }
                }

                path.FindbugsResults.FindBugsSummary.'total_bugs' = bugCount   // Fixes visitor problem

                xmlProject.appendNode {
                    WrkDir(project.build.directory)
                }

                def xmlBuilder = new StreamingMarkupBuilder()

                if (outputFile.exists()) {
                    outputFile.delete()
                }

                outputFile.getParentFile().mkdirs()
                outputFile.createNewFile()

                outputFile.write "\n"

                outputFile << xmlBuilder.bind { mkp.yield path }
            } else {
                log.info("No bugs found")
            }

            if (!log.isDebugEnabled()) {
                tempFile.delete()
            }

        }

        if (outputFile.exists()) {

            log.debug("xmlOutput is ${xmlOutput}")


            if (xmlOutput) {
                log.debug("  Using the xdoc format")

                if (!xmlOutputDirectory.exists()) {
                    if (!xmlOutputDirectory.mkdirs()) {
                        fail("Cannot create xdoc output directory")
                    }
                }

                XDocsReporter xDocsReporter = new XDocsReporter(getBundle(locale), log, threshold, effort, outputEncoding)
                xDocsReporter.setOutputWriter(new OutputStreamWriter(new FileOutputStream(new File("${xmlOutputDirectory}/findbugs.xml")), outputEncoding))
                xDocsReporter.setFindbugsResults(new XmlSlurper().parse(outputFile))
                xDocsReporter.setCompileSourceRoots(this.compileSourceRoots)
                xDocsReporter.setTestSourceRoots(this.testSourceRoots)

                xDocsReporter.generateReport()
            }
        }

    }

    /**
     * Returns the threshold parameter to use.
     *
     * @return A valid threshold parameter.
     *
     */
    protected String getThresholdParameter() {

        log.debug("threshold is ${threshold}")

        String thresholdParameter

        switch (threshold) {
            case "High":
                thresholdParameter = "-high"; break

            case "Exp":
                thresholdParameter = "-experimental"; break

            case "Low":
                thresholdParameter = "-low"; break

            case "high":
                thresholdParameter = "-high"; break

            default:
                thresholdParameter = "-medium"; break
        }
        log.debug("thresholdParameter is ${thresholdParameter}")

        return thresholdParameter

    }

    /**
     * Returns the effort parameter to use.
     *
     * @return A valid effort parameter.
     *
     */
    protected String getEffortParameter() {
        log.debug("effort is ${effort}")

        String effortParameter

        switch (effort) {
            case "Max":
                effortParameter = "max"; break

            case "Min":
                effortParameter = "min"; break

            default:
                effortParameter = "default"; break
        }

        log.debug("effortParameter is ${effortParameter}")

        return "-effort:" + effortParameter
    }

    /**
     * Get the File reference for a File passed in as a string reference.
     *
     * @param resource
     *            The file for the resource manager to locate
     * @return The File of the resource
     *
     */
    protected File getResourceFile(String resource) {

        assert resource

        String location = null
        String artifact = resource

        if (resource.indexOf(FindBugsInfo.FORWARD_SLASH) != -1) {
            artifact = resource.substring(resource.lastIndexOf(FindBugsInfo.FORWARD_SLASH) + 1)
        }

        if (resource.indexOf(FindBugsInfo.FORWARD_SLASH) != -1) {
            location = resource.substring(0, resource.lastIndexOf(FindBugsInfo.FORWARD_SLASH))
        }

        // replace all occurrences of the following characters:  ? : & =
        location = location?.replaceAll("[\\?\\:\\&\\=\\%]", "_")
        artifact = artifact?.replaceAll("[\\?\\:\\&\\=\\%]", "_")

        log.debug("resource is " + resource)
        log.debug("location is " + location)
        log.debug("artifact is " + artifact)

        //		File resourceFile = resourceManager.getResourceAsFile(resource, artifact)
        File resourceFile = getResourceAsFile(resource, artifact, findbugsXmlOutputDirectory)

        log.debug("location of resourceFile file is " + resourceFile.absolutePath)

        return resourceFile

    }

    /**
     * Adds the specified plugins to findbugs. The coreplugin is always added first.
     *
     */
    protected String getFindbugsPlugins() {
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

                    urlPlugins += getResourceFile(pluginFileName.toString()).absolutePath + ((pluginJar == pluginJars[pluginJars.size() - 1]) ? "" : File.pathSeparator)
                } catch (MalformedURLException exception) {
                    fail("The addin plugin has an invalid URL", exception)
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

                urlPlugins += getResourceFile(pomArtifact.file.absolutePath).absolutePath + ((plugin == plugins[plugins.size() - 1]) ? "" : File.pathSeparator)
            }
        }


        log.debug("  Plugin list is: ${urlPlugins}")

        return urlPlugins
    }

    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#setReportOutputDirectory(java.io.File)
     */
    public void setReportOutputDirectory(File reportOutputDirectory) {
        super.setReportOutputDirectory(reportOutputDirectory)
        this.outputDirectory = reportOutputDirectory
    }

    /**
     * Collects the java sources from the source roots.
     *
     * @return A list containing the java sources or an empty list if no java sources are found.
     *
     */
    protected List getJavaSources(Locale locale) {
        List sourceFiles = new ArrayList()

        if (classFilesDirectory.exists() && classFilesDirectory.isDirectory()) {
            List files = FileUtils.getFiles(classFilesDirectory, FindBugsInfo.JAVA_REGEX_PATTERN, null)
            sourceFiles.addAll(files)
        }

        if (testClassFilesDirectory.exists() && testClassFilesDirectory.isDirectory() && includeTests) {
            List files = FileUtils.getFiles(testClassFilesDirectory, FindBugsInfo.JAVA_REGEX_PATTERN, null)
            sourceFiles.addAll(files)
        }

        return sourceFiles
    }

    File getResourceAsFile(String name, String outputPath, File outputDirectory) {
        // Optimization for File to File fetches
        File f = FileResourceLoader.getResourceAsFile(name, outputPath, outputDirectory)

        if (f != null) {
            return f
        }

        // End optimization

        InputStream is = new BufferedInputStream(resourceManager.getResourceAsInputStream(name))

        File outputFile

        if (outputPath == null) {
            outputFile = FileUtils.createTempFile("plexus-resources", "tmp", null)
        } else {
            if (outputDirectory != null) {
                outputFile = new File(outputDirectory, outputPath)
            } else {
                outputFile = new File(outputPath)
            }
        }

        try {
            if (!outputFile.getParentFile().exists()) {
                outputFile.getParentFile().mkdirs()
            }

            def os = new FileOutputStream(outputFile)


            os << is


        } catch (IOException e) {
            throw new FileResourceCreationException("Cannot create file-based resource.", e)
        } finally {
            is.close()
        }

        return outputFile
    }

}

class PluginArtifact {
    String groupId, artifactId, version

    String type = "jar"
}

