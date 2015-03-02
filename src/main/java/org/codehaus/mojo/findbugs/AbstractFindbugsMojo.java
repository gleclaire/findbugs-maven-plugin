package org.codehaus.mojo.findbugs;

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

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.doxia.tools.SiteTool;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.codehaus.mojo.findbugs.PluginArtifact;
import org.codehaus.plexus.resource.ResourceManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by garvin on 2/10/15.
 */
public abstract class AbstractFindbugsMojo extends AbstractMavenReport {

    /**
     * Location where generated html will be created.
     *
     */

    @Parameter( defaultValue = "${project.reporting.outputDirectory}", required = true )
    public File outputDirectory;

    /**
     * Turn on and off xml output of the Findbugs report.
     *
     * @since 1.0.0
     */
    @Parameter( defaultValue = "false", property="findbugs.xmlOutput", required = true )
    public boolean xmlOutput;

    /**
     * Specifies the directory where the xml output will be generated.
     *
     * @since 1.0.0
     */
    @Parameter( defaultValue = "${project.build.directory}", required = true )
    public File xmlOutputDirectory;

    /**
     * This has been deprecated and is on by default.
     *
     * @since 1.2.0
     *
     */
    @Deprecated
    @Parameter( defaultValue = "true" )
    public boolean findbugsXmlOutput;

    /**
     * Specifies the directory where the findbugs native xml output will be generated.
     *
     * @since 1.2.0
     */
    @Parameter( defaultValue = "${project.build.directory}", required = true )
    public File findbugsXmlOutputDirectory;

    /**
     * Doxia Site Renderer.
     *
     * @component
     *
     */
    @Component( role = Renderer.class)
    public Renderer siteRenderer;

    /**
     * Directory containing the class files for FindBugs to analyze.
     *
     * @required
     */
    @Parameter( defaultValue = "${project.build.outputDirectory}", required = true )
    public File classFilesDirectory;

    /**
     * Directory containing the test class files for FindBugs to analyze.
     *
     */
    @Parameter( defaultValue = "${project.build.testOutputDirectory}", required = true )
    public File testClassFilesDirectory;

    /**
     * Location of the Xrefs to link to.
     *
     */
    @Parameter( defaultValue = "${project.reporting.outputDirectory}/xref" )
    public File xrefLocation;

    /**
     * Location of the Test Xrefs to link to.
     *
     */
    @Parameter( defaultValue = "${project.reporting.outputDirectory}/xref-test" )
    public File xrefTestLocation;

    /**
     * The directories containing the sources to be compiled.
     *
     */
    @Parameter( defaultValue = "${project.compileSourceRoots}", required = true, readonly = true )
    public List compileSourceRoots;

    /**
     * The directories containing the test-sources to be compiled.
     *
     * @since 2.0
     */
    @Parameter( defaultValue = "${project.testCompileSourceRoots}", required = true, readonly = true )
    public List testSourceRoots;

    /**
     * Run Findbugs on the tests.
     *
     * @since 2.0
     */
    @Parameter( defaultValue = "false", property="findbugs.includeTests" )
    public boolean includeTests;

    /**
     * List of artifacts this plugin depends on. Used for resolving the Findbugs coreplugin.
     *
     */
    @Parameter( property="plugin.artifacts", required = true, readonly = true )
    public ArrayList pluginArtifacts;

    /**
     * List of Remote Repositories used by the resolver
     *
     */
    @Parameter( property="project.remoteArtifactRepositories", required = true, readonly = true )
    public List remoteRepositories;

    /**
     * The local repository, needed to download the coreplugin jar.
     *
     */
    @Parameter( property="localRepository", required = true, readonly = true )
    public ArtifactRepository localRepository;

    /**
     * Remote repositories which will be searched for the coreplugin jar.
     *
     */
    @Parameter( property="project.remoteArtifactRepositories", required = true, readonly = true )
    public List remoteArtifactRepositories;

    /**
     * Maven Project
     *
     */
    @Parameter( property="project", required = true, readonly = true )
    public MavenProject project;

    /**
     * Encoding used for xml files. Default value is UTF-8.
     *
     */
    @Parameter( defaultValue = "UTF-8", readonly = true )
    public String xmlEncoding;

    /**
     * The file encoding to use when reading the source files. If the property <code>project.build.sourceEncoding</code>
     * is not set, the platform default encoding is used.
     *
     * @since 2.2
     */
    @Parameter( defaultValue = "${project.build.sourceEncoding}", property="encoding" )
    public String sourceEncoding;

    /**
     * The file encoding to use when creating the HTML reports. If the property <code>project.reporting.outputEncoding</code>
     * is not set, the platform default encoding is used.
     *
     * @since 2.2
     */
    @Parameter( defaultValue = "${project.reporting.outputEncoding}", property="outputEncoding" )
    public String outputEncoding;

    /**
     * Threshold of minimum bug severity to report. Valid values are High, Default, Low, Ignore, and Exp (for experimental).
     *
     */
    @Parameter( defaultValue = "Default", property="findbugs.threshold" )
    public String threshold;

    /**
     * Artifact resolver, needed to download the coreplugin jar.
     *
     * @required
     * @readonly
     */
    @Component( role = org.apache.maven.artifact.resolver.ArtifactResolver.class )
    public ArtifactResolver artifactResolver;

    /**
     * Used to look up Artifacts in the remote repository.
     *
     */
    @Parameter( property="component.org.apache.maven.artifact.factory.ArtifactFactory", required = true, readonly = true )
    public ArtifactFactory factory;

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
     * @since 1.0-beta-1
     */
    @Parameter( property="findbugs.includeFilterFile" )
    public String includeFilterFile;

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
     * @since 1.0-beta-1
     */
    @Parameter( property="findbugs.excludeFilterFile" )
    public String excludeFilterFile;

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
     * @since 2.4.1
     */
    @Parameter( property="findbugs.excludeBugsFile" )
    public String excludeBugsFile;

    /**
     * Effort of the bug finders. Valid values are Min, Default and Max.
     *
     * @since 1.0-beta-1
     */
    @Parameter( defaultValue = "Default", property="findbugs.effort" )
    public String effort;

    /**
     * turn on Findbugs debugging
     *
     */
    @Parameter( defaultValue = "false", property="findbugs.debug" )
    public Boolean debug;

    /**
     * Relaxed reporting mode. For many detectors, this option suppresses the heuristics used to avoid reporting false
     * positives.
     *
     * @since 1.1
     */
    @Parameter( defaultValue = "false", property="findbugs.relaxed" )
    public Boolean relaxed;

    /**
     * The visitor list to run. This is a comma-delimited list.
     *
     * @since 1.0-beta-1
     */
    @Parameter( property="findbugs.visitors" )
    public String visitors;

    /**
     * The visitor list to omit. This is a comma-delimited list.
     *
     * @since 1.0-beta-1
     */
    @Parameter( property="findbugs.omitVisitors" )
    public String omitVisitors;

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
     * @since 1.0-beta-1
     */
    @Parameter( property="findbugs.pluginList" )
    public String pluginList;

    /**
     * <p>
     * Collection of PluginArtifact to work on. (PluginArtifact contains groupId, artifactId, version, type.)
     * See <a href="./usage.html#Using Detectors from a Repository">Usage</a> for details.
     * </p>
     *
     *
     * @since 2.4.1
     */
    @Parameter
    public PluginArtifact[] plugins;

    /**
     * Restrict analysis to the given comma-separated list of classes and packages.
     *
     * @since 1.1
     */
    @Parameter( property="findbugs.onlyAnalyze" )
    public String onlyAnalyze;

    /**
     * This option enables or disables scanning of nested jar and zip files found
     *  in the list of files and directories to be analyzed.
     *
     * @since 2.3.2
     */
    @Parameter( property="findbugs.nested", defaultValue = "false" )
    public Boolean nested;

    /**
     * Prints a trace of detectors run and classes analyzed to standard output.
     * Useful for troubleshooting unexpected analysis failures.
     *
     * @since 2.3.2
     */
    @Parameter( property="findbugs.trace", defaultValue = "false" )
    public Boolean trace;

    /**
     * Maximum bug ranking to record.
     *
     * @since 2.4.1
     */
    @Parameter( property="findbugs.maxRank" )
    public int maxRank;

    /**
     * Skip entire check.
     *
     * @since 1.1
     */
    @Parameter( property="findbugs.skip", defaultValue = "false" )
    public boolean skip;

    /**
     * @required
     * @readonly
     * @since 2.0
     */
    @Component( role = ResourceManager.class)
    public ResourceManager resourceManager;

    /**
     * SiteTool.
     *
     * @since 2.1-SNAPSHOT
     * @required
     * @readonly
     */
    @Component( role = org.apache.maven.doxia.tools.SiteTool.class)
    public SiteTool siteTool;

    /**
     * Fail the build on an error.
     *
     * @since 2.0
     */
    @Parameter( property="findbugs.failOnError", defaultValue = "true" )
    public boolean failOnError;

    /**
     * Fork a VM for FindBugs analysis.  This will allow you to set timeouts and heap size
     *
     * @since 2.3.2
     */
    @Parameter( property="findbugs.fork", defaultValue = "true" )
    public boolean fork;

    /**
     * Maximum Java heap size in megabytes  (default=512).
     * This only works if the <b>fork</b> parameter is set <b>true</b>.
     *
     * @since 2.2
     */
    @Parameter( property="findbugs.maxHeap", defaultValue = "512" )
    public int maxHeap;

    /**
     * Specifies the amount of time, in milliseconds, that FindBugs may run before
     *  it is assumed to be hung and is terminated.
     * The default is 600,000 milliseconds, which is ten minutes.
     * This only works if the <b>fork</b> parameter is set <b>true</b>.
     *
     * @since 2.2
     */
    @Parameter( property="findbugs.timeout", defaultValue = "600000" )
    public int timeout;

    /**
     * <p>
     * the arguments to pass to the forked VM (ignored if fork is disabled).
     * </p>
     *
     * @since 2.4.1
     */
    @Parameter( property="findbugs.jvmArgs" )
    public String jvmArgs;


    /**
     * Return the project.
     *
     * @return the project.
     * @see org.apache.maven.reporting.AbstractMavenReport#getProject()
     */
    @Override
    protected MavenProject getProject() {
        return project;
    }

    /**
     * Returns the report output directory.
     *
     * Called by AbstractMavenReport.execute() for creating the sink.
     *
     * @return full path to the directory where the files in the site get copied to
     * @see org.apache.maven.reporting.AbstractMavenReport#getOutputDirectory()
     */
    @Override
    protected String getOutputDirectory() {
        return outputDirectory.getAbsolutePath();
    }

}
