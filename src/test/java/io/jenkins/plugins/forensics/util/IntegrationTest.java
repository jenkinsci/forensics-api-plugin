package io.jenkins.plugins.forensics.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.JSONWebResponse;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlFormUtil;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.FilePath;
import hudson.Functions;
import hudson.model.Action;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.Slave;
import hudson.model.TopLevelItem;
import hudson.model.labels.LabelAtom;
import hudson.slaves.DumbSlave;
import hudson.tasks.BatchFile;
import hudson.tasks.Builder;
import hudson.tasks.Shell;
import jenkins.model.ParameterizedJobMixIn.ParameterizedJob;
import jenkins.security.s2m.AdminWhitelistRule;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assumptions.*;

/**
 * Base class for integration tests in Jenkins.
 *
 * @author Ullrich Hafner
 */
@Tag("IntegrationTest")
@SuppressWarnings({"ClassDataAbstractionCoupling", "ClassFanOutComplexity", "SameParameterValue", "PMD.SystemPrintln", "PMD.GodClass", "PMD.ExcessiveClassLength", "PMD.ExcessiveImports", "PMD.CouplingBetweenObjects", "PMD.CyclomaticComplexity"})
public abstract class IntegrationTest extends ResourceTest {
    /** Issue log files will be renamed to mach this pattern. */
    private static final String FILE_NAME_PATTERN = "%s-issues.txt";
    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    /** Step to publish a set of issues. Uses defaults for all options. */
    protected static final String PUBLISH_ISSUES_STEP = "publishIssues issues:[issues]";
    private static final String WINDOWS_FILE_ACCESS_READ_ONLY = "RX";
    private static final String WINDOWS_FILE_DENY = "/deny";

    /** Determines whether JavaScript is enabled in the {@link WebClient}. */
    public enum JavaScriptSupport {
        /** JavaScript is disabled. */
        JS_ENABLED,
        /** JavaScript is enabled. */
        JS_DISABLED
    }

    /**
     * Returns the Jenkins rule to manage the Jenkins instance.
     *
     * @return Jenkins rule
     */
    protected abstract JenkinsRule getJenkins();

    /**
     * Returns a {@link WebClient} to access the HTML pages of Jenkins.
     *
     * @param javaScriptSupport
     *         determines whether JavaScript is enabled in the {@link WebClient}
     *
     * @return the web client to use
     */
    protected abstract WebClient getWebClient(JavaScriptSupport javaScriptSupport);

    @SuppressFBWarnings(value = "LG", justification = "Setting the logger here helps to clean up the console log for tests")
    static WebClient create(final JenkinsRule jenkins, final boolean isJavaScriptEnabled) {
        WebClient webClient = jenkins.createWebClient();
        webClient.setCssErrorHandler(new SilentCssErrorHandler());
        java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.SEVERE);
        webClient.setIncorrectnessListener((s, o) -> {
        });

        webClient.setJavaScriptEnabled(isJavaScriptEnabled);
        webClient.setJavaScriptErrorListener(new IntegrationTestJavaScriptErrorListener());
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        webClient.getCookieManager().setCookiesEnabled(isJavaScriptEnabled);
        webClient.getOptions().setCssEnabled(isJavaScriptEnabled);

        webClient.getOptions().setDownloadImages(false);
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);

        return webClient;
    }

    /**
     * Creates a file with the specified content in the workspace.
     *
     * @param job
     *         the job to get the workspace for
     * @param fileName
     *         the files to create
     * @param content
     *         the content of the file
     */
    protected void createFileInWorkspace(final TopLevelItem job, final String fileName, final String content) {
        try {
            FilePath workspace = getWorkspace(job);

            FilePath child = workspace.child(fileName);
            child.copyFrom(new ByteArrayInputStream(content.getBytes(UTF_8)));
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Copies the specified files to the workspace using a generated file name that uses the same suffix. So the pattern
     * in the static analysis configuration can use the same fixed regular expression for all types of tools.
     *
     * @param job
     *         the job to get the workspace for
     * @param fileNames
     *         the files to copy
     *
     * @see #FILE_NAME_PATTERN
     */
    protected void copyMultipleFilesToWorkspaceWithSuffix(final TopLevelItem job, final String... fileNames) {
        copyWorkspaceFiles(job, fileNames, this::createWorkspaceFileName);
    }

    /**
     * Copies the specified files to the workspace. The copied files will have the same file name in the workspace.
     *
     * @param job
     *         the job to get the workspace for
     * @param fileNames
     *         the files to copy
     */
    protected void copyMultipleFilesToWorkspace(final TopLevelItem job, final String... fileNames) {
        copyWorkspaceFiles(job, fileNames, file -> Paths.get(file).getFileName().toString());
    }

    /**
     * Copies the specified file to the workspace.
     *
     * @param job
     *         the job to get the workspace for
     * @param fileName
     *         the file to copy
     */
    protected void copySingleFileToWorkspace(final TopLevelItem job, final String fileName) {
        FilePath workspace = getWorkspace(job);

        copySingleFileToWorkspace(workspace, fileName, fileName);
    }

    /**
     * Copies the specified files to the workspace. Uses the specified new file name in the workspace.
     *
     * @param job
     *         the job to get the workspace for
     * @param from
     *         the file to copy
     * @param to
     *         the file name in the workspace
     */
    protected void copySingleFileToWorkspace(final TopLevelItem job, final String from, final String to) {
        FilePath workspace = getWorkspace(job);

        copySingleFileToWorkspace(workspace, from, to);
    }

    /**
     * Copies the specified directory recursively to the workspace.
     *
     * @param job
     *         the job to get the workspace for
     * @param directory
     *         the directory to copy
     */
    protected void copyDirectoryToWorkspace(final TopLevelItem job, final String directory) {
        try {
            URL resource = getTestResourceClass().getResource(directory);
            assertThat(resource).as("No such file: %s", directory).isNotNull();
            FilePath destination = new FilePath(new File(resource.getFile()));
            assertThat(destination.exists()).as("Directory %s does not exist", resource.getFile()).isTrue();
            destination.copyRecursiveTo(getWorkspace(job));
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Copies the specified files to the workspace. The file names of the copied files will be determined by the
     * specified mapper.
     *
     * @param job
     *         the job to get the workspace for
     * @param fileNames
     *         the files to copy
     * @param fileNameMapper
     *         maps input file names to output file names
     */
    protected void copyWorkspaceFiles(final TopLevelItem job, final String[] fileNames,
            final Function<String, String> fileNameMapper) {
        Arrays.stream(fileNames)
                .forEach(fileName -> copySingleFileToWorkspace(job, fileName, fileNameMapper.apply(fileName)));
    }

    /**
     * Returns the workspace for the specified job.
     *
     * @param job
     *         the job to get the workspace for
     *
     * @return the workspace
     */
    protected FilePath getWorkspace(final TopLevelItem job) {
        FilePath workspace = getJenkins().jenkins.getWorkspaceFor(job);
        assertThat(workspace).isNotNull();
        return workspace;
    }

    /**
     * Returns the absolute path of the specified file.
     *
     * @param job
     *         the job with the workspace that contains the file
     * @param fileName
     *         the file name
     *
     * @return the workspace
     */
    protected String getAbsolutePathOfWorkspaceFile(final TopLevelItem job, final String fileName) {
        try {
            return Paths.get(getWorkspace(job).child(fileName).getRemote())
                    .toAbsolutePath()
                    .toRealPath(LinkOption.NOFOLLOW_LINKS)
                    .toString()
                    .replace('\\', '/');
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private void copySingleFileToWorkspace(final FilePath workspace, final String from, final String to) {
        try {
            workspace.child(to).copyFrom(asInputStream(from));
            System.out.format("Copying file '%s' as workspace file '%s'%n", from, to);
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Creates an {@link DumbSlave agent} with the specified label.
     *
     * @param label
     *         the label of the agent
     *
     * @return the agent
     */
    @SuppressWarnings("illegalcatch")
    protected Slave createAgent(final String label) {
        try {
            return getJenkins().createOnlineSlave(new LabelAtom(label));
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Creates an {@link DumbSlave agent} with the specified label. Master - agent security will be enabled.
     *
     * @param label
     *         the label of the agent
     *
     * @return the agent
     */
    protected Slave createAgentWithEnabledSecurity(final String label) {
        try {
            Slave agent = createAgent(label);

            FilePath child = getJenkins().getInstance()
                    .getRootPath()
                    .child("secrets/filepath-filters.d/30-default.conf");
            child.delete();
            child.write("", "ISO_8859_1");

            Objects.requireNonNull(getJenkins().jenkins.getInjector())
                    .getInstance(AdminWhitelistRule.class).setMasterKillSwitch(false);
            getJenkins().jenkins.save();
            return agent;
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Copies the specified files to the workspace of the specified agent. Uses the specified new file name in the
     * workspace.
     *
     * @param agent
     *         the agent to get the workspace for
     * @param job
     *         the job to get the workspace for
     * @param from
     *         the file to copy
     * @param to
     *         the file name in the workspace
     */
    protected void copySingleFileToAgentWorkspace(final Slave agent, final TopLevelItem job,
            final String from, final String to) {
        FilePath workspace = getAgentWorkspace(agent, job);

        copySingleFileToWorkspace(workspace, from, to);
    }

    /**
     * Returns the agent workspace of a job.
     *
     * @param agent
     *         the agent
     * @param job
     *         the job
     *
     * @return path to the workspace
     */
    protected FilePath getAgentWorkspace(final Slave agent, final TopLevelItem job) {
        FilePath workspace = agent.getWorkspaceFor(job);
        assertThat(workspace).isNotNull();
        return workspace;
    }

    /**
     * Creates the specified file with the given content to the workspace of the specified agent.
     *
     * @param agent
     *         the agent to get the workspace for
     * @param job
     *         the job to get the workspace for
     * @param fileName
     *         the file name
     * @param content
     *         the content to write
     */
    protected void createFileInAgentWorkspace(final Slave agent, final TopLevelItem job, final String fileName,
            final String content) {
        try {
            FilePath workspace = getAgentWorkspace(agent, job);
            FilePath child = workspace.child(fileName);
            child.copyFrom(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Creates a pre-defined filename for a workspace file.
     *
     * @param fileNamePrefix
     *         prefix of the filename
     *
     * @return the whole file name of the workspace file
     */
    protected String createWorkspaceFileName(final String fileNamePrefix) {
        return String.format(FILE_NAME_PATTERN, FilenameUtils.getBaseName(fileNamePrefix));
    }

    /**
     * Creates a new {@link FreeStyleProject freestyle job}. The job will get a generated name.
     *
     * @return the created job
     */
    protected FreeStyleProject createFreeStyleProject() {
        return createProject(FreeStyleProject.class);
    }

    /**
     * Creates a new {@link FreeStyleProject freestyle job} and copies the specified resources to the workspace folder.
     * The job will get a generated name.
     *
     * @param fileNames
     *         the files to copy to the workspace
     *
     * @return the created job
     */
    protected FreeStyleProject createFreeStyleProjectWithWorkspaceFiles(final String... fileNames) {
        FreeStyleProject job = createFreeStyleProject();
        copyMultipleFilesToWorkspaceWithSuffix(job, fileNames);
        return job;
    }

    /**
     * Creates a new job of the specified type. The job will get a generated name.
     *
     * @param type
     *         type of the job
     * @param <T>
     *         the project type
     *
     * @return the created job
     */
    protected <T extends TopLevelItem> T createProject(final Class<T> type) {
        try {
            return getJenkins().createProject(type);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Creates a new job of the specified type.
     *
     * @param type
     *         type of the job
     * @param name
     *         the name of the job
     * @param <T>
     *         the project type
     *
     * @return the created job
     */
    protected <T extends TopLevelItem> T createProject(final Class<T> type, final String name) {
        try {
            return getJenkins().createProject(type, name);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Wraps the specified steps into a stage.
     *
     * @param steps
     *         the steps of the stage
     *
     * @return the pipeline script
     */
    @SuppressWarnings({"UseOfSystemOutOrSystemErr", "PMD.ConsecutiveLiteralAppends"})
    protected CpsFlowDefinition asStage(final String... steps) {
        StringBuilder script = new StringBuilder(1024);
        script.append("node {\n");
        script.append("  stage ('Integration Test') {\n");
        for (String step : steps) {
            script.append("    ");
            script.append(step);
            script.append('\n');
        }
        script.append("  }\n");
        script.append("}\n");

        String jenkinsFile = script.toString();
        logJenkinsFile(jenkinsFile);
        return new CpsFlowDefinition(jenkinsFile, true);
    }

    /**
     * Creates an empty pipeline job and populates the workspace of that job with copies of the specified files. In
     * order to simplify the scanner pattern, all files follow the filename pattern in {@link
     * IntegrationTest#createWorkspaceFileName(String)}.
     *
     * @param fileNames
     *         the files to copy to the workspace
     *
     * @return the pipeline job
     */
    protected WorkflowJob createPipelineWithWorkspaceFiles(final String... fileNames) {
        WorkflowJob job = createPipeline();
        copyMultipleFilesToWorkspaceWithSuffix(job, fileNames);
        return job;
    }

    /**
     * Creates an empty pipeline job.
     *
     * @return the pipeline job
     */
    protected WorkflowJob createPipeline() {
        return createProject(WorkflowJob.class);
    }

    /**
     * Creates an empty pipeline job with the specified name.
     *
     * @param name
     *         the name of the job
     *
     * @return the pipeline job
     */
    protected WorkflowJob createPipeline(final String name) {
        return createProject(WorkflowJob.class, name);
    }

    /**
     * Reads a JenkinsFile (i.e. a {@link FlowDefinition}) from the specified file.
     *
     * @param fileName
     *         path to the JenkinsFile
     *
     * @return the JenkinsFile as {@link FlowDefinition} instance
     */
    protected FlowDefinition readJenkinsFile(final String fileName) {
        String script = toString(fileName);
        logJenkinsFile(script);
        return new CpsFlowDefinition(script, true);
    }

   /**
     * Schedules a build for the specified job and waits for the job to finish. After the build has been finished the
     * builds result is checked to be equals to {@link Result#SUCCESS}.
     *
     * @param job
     *         the job to schedule
     *
     * @return the finished build with status {@link Result#SUCCESS}
     */
    protected Run<?, ?> buildSuccessfully(final ParameterizedJob<?, ?> job) {
        return buildWithResult(job, Result.SUCCESS);
    }

    /**
     * Schedules a build for the specified job and waits for the job to finish. After the build has been finished the
     * builds result is checked to be equals to {@code expectedResult}.
     *
     * @param job
     *         the job to schedule
     * @param expectedResult
     *         the expected result for the build
     *
     * @return the finished build with status {@code expectedResult}
     */
    @SuppressWarnings({"illegalcatch", "OverlyBroadCatchBlock"})
    protected Run<?, ?> buildWithResult(final ParameterizedJob<?, ?> job, final Result expectedResult) {
        try {
            return getJenkins().assertBuildStatus(expectedResult,
                    Objects.requireNonNull(job.scheduleBuild2(0, new Action[0])));
        }
        catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Prints the content of the JenkinsFile to StdOut.
     *
     * @param script
     *         the script
     */
    @SuppressWarnings("PMD.SystemPrintln")
    private void logJenkinsFile(final String script) {
        System.out.println("----------------------------------------------------------------------");
        System.out.println(script);
        System.out.println("----------------------------------------------------------------------");
    }

    /**
     * Clicks the specified DOM element and returns the HTML page content of the page that is the target of the link.
     *
     * @param element
     *         the element that receives the click event
     *
     * @return the HTML page
     */
    protected HtmlPage clickOnLink(final DomElement element) {
        try {
            return element.click();
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns the HTML page content of the specified URL for a given job.
     *
     * @param javaScriptSupport
     *         determines whether JavaScript is enabled in the {@link WebClient}
     * @param job
     *         the job that owns the URL
     * @param relativeUrl
     *         the relative URL within the job
     *
     * @return the HTML page
     */
    protected HtmlPage getWebPage(final JavaScriptSupport javaScriptSupport, final Item job, final String relativeUrl) {
        try {
            return getWebClient(javaScriptSupport).getPage(job, relativeUrl);
        }
        catch (SAXException | IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns the HTML page content of the specified job.
     *
     * @param javaScriptSupport
     *         determines whether JavaScript is enabled in the {@link WebClient}
     * @param job
     *         the job to show the page for
     *
     * @return the HTML page
     */
    protected HtmlPage getWebPage(final JavaScriptSupport javaScriptSupport, final Item job) {
        return getWebPage(javaScriptSupport, job, StringUtils.EMPTY);
    }

    /**
     * Returns the HTML page content of the specified build.
     *
     * @param javaScriptSupport
     *         determines whether JavaScript is enabled in the {@link WebClient}
     * @param build
     *         the build to show the page for
     *
     * @return the HTML page
     */
    protected HtmlPage getWebPage(final JavaScriptSupport javaScriptSupport, final Run<?, ?> build) {
        return getWebPage(javaScriptSupport, build, StringUtils.EMPTY);
    }

    /**
     * Returns the HTML page content of the specified URL for a given build.
     *
     * @param javaScriptSupport
     *         determines whether JavaScript is enabled in the {@link WebClient}
     * @param build
     *         the build to show the page for
     * @param relativeUrl
     *         the relative URL within the job
     *
     * @return the HTML page
     */
    protected HtmlPage getWebPage(final JavaScriptSupport javaScriptSupport, final Run<?, ?> build,
            final String relativeUrl) {
        try {
            return getWebClient(javaScriptSupport).getPage(build, relativeUrl);
        }
        catch (SAXException | IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Submit the supplied {@link HtmlForm}. Locates the submit element/button on the form.
     *
     * @param form
     *         the {@link HtmlForm}
     */
    protected void submit(final HtmlForm form) {
        try {
            HtmlFormUtil.submit(form);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Makes the specified file unreadable.
     *
     * @param file
     *         the specified file
     */
    protected void makeFileUnreadable(final Path file) {
        makeFileUnreadable(file.toString());
    }

    /**
     * Makes the specified file unreadable.
     *
     * @param absolutePath
     *         the specified file
     */
    protected void makeFileUnreadable(final String absolutePath) {
        File nonReadableFile = new File(absolutePath);
        if (Functions.isWindows()) {
            setAccessModeOnWindows(absolutePath, WINDOWS_FILE_DENY, WINDOWS_FILE_ACCESS_READ_ONLY);
        }
        else {
            assertThat(nonReadableFile.setReadable(false, false)).isTrue();
            assumeThat(nonReadableFile.canRead())
                    .as("File ´%s´ could not be made unreadable (OS configuration problem?)", absolutePath)
                    .isFalse();
        }
    }

    private void setAccessModeOnWindows(final String path, final String command, final String accessMode) {
        try {
            Process process = Runtime.getRuntime()
                    .exec("icacls \"" + path + "\" " + command + " *S-1-1-0:" + accessMode);
            process.waitFor();
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Adds a script as a {@link Shell} or {@link BatchFile} depending on the current OS.
     *
     * @param project
     *         the project
     * @param script
     *         the script to run
     *
     * @return the created script step
     */
    protected Builder addScriptStep(final FreeStyleProject project, final String script) {
        Builder item;
        if (Functions.isWindows()) {
            item = new BatchFile(script);
        }
        else {
            item = new Shell(script);
        }
        project.getBuildersList().add(item);
        return item;
    }

    /**
     * Cleans the workspace of the specified job. Deletes all files in the workspace.
     *
     * @param job
     *         the workspace to clean
     */
    protected void cleanWorkspace(final TopLevelItem job) {
        try {
            getWorkspace(job).deleteContents();
        }
        catch (IOException | InterruptedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Add a build step that simply fails the build.
     *
     * @param project
     *         the job to add the step
     *
     * @return the created build step
     */
    protected Builder addFailureStep(final FreeStyleProject project) {
        return addScriptStep(project, "exit 1");
    }

    /**
     * Removes the specified builder from the list of registered builders.
     *
     * @param project
     *         the job to add the step
     * @param builder
     *         the builder to remove
     */
    protected void removeBuilder(final FreeStyleProject project, final Builder builder) {
        project.getBuildersList().remove(builder);
    }

    /**
     * Joins the specified arguments as list of comma separated values. Note that the first element is separated with a
     * comma as well.
     * <blockquote>For example,
     * <pre>{@code
     *     String message = join("Java", "is", "cool");
     *     // message returned is: ",Java,is,cool"
     * }</pre></blockquote>
     *
     * @param arguments
     *         th arguments to join
     *
     * @return the concatenated string
     */
    protected String join(final String... arguments) {
        StringBuilder builder = new StringBuilder();
        for (String argument : arguments) {
            builder.append(", ");
            builder.append(argument);
        }
        return builder.toString();
    }

    /**
     * Returns the model of a chart in the specified HTML page.
     *
     * @param page
     *         the HTML page that contains the chart
     * @param id
     *         the element ID of the chart placeholder (that has the EChart instance attached in property @{@code
     *         echart}
     *
     * @return the model (as JSON representation)
     */
    protected String getChartModel(final HtmlPage page, final String id) {
        ScriptResult scriptResult = page.executeJavaScript(
                String.format("JSON.stringify(document.getElementById(\"%s\").echart.getOption());", id));

        return scriptResult.getJavaScriptResult().toString();
    }

    /**
     * Calls Jenkins remote API with the specified URL. Calls the JSON format.
     *
     * @param url
     *         the URL to call
     *
     * @return the JSON response
     */
    protected JSONWebResponse callJsonRemoteApi(final String url) {
        try {
            return getJenkins().getJSON(url);
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Calls Jenkins remote API with the specified URL. Calls the XML format.
     *
     * @param url
     *         the URL to call
     *
     * @return the XML response
     */
    protected Document callXmlRemoteApi(final String url) {
        try {
            return getJenkins().createWebClient().goToXml(url).getXmlDocument();
        }
        catch (IOException | SAXException e) {
            throw new AssertionError(e);
        }
    }

    private static class IntegrationTestJavaScriptErrorListener implements JavaScriptErrorListener {
        /**
         * Informs about a javascript exceptions.
         *
         * @param page
         *         the page that causes the problem
         * @param scriptException
         *         the occurred script exception
         */
        @Override
        @SuppressWarnings("PMD.SystemPrintln")
        public void scriptException(final HtmlPage page, final ScriptException scriptException) {
            System.out.println("A JavaScript exception occurred at: " + page.toString());
            scriptException.printStackTrace();
        }

        /**
         * Informs about a javascript timeout error.
         *
         * @param page
         *         the page that causes the problem
         * @param allowedTime
         *         the max time allowed for the execution
         * @param executionTime
         *         the already consumed time
         */
        @Override
        @SuppressWarnings("PMD.SystemPrintln")
        public void timeoutError(final HtmlPage page, final long allowedTime, final long executionTime) {
            System.out.println("A JavaScript timeout occurred at: " + page.toString() + ". Allowed: "
                    + allowedTime + " timed out after: " + executionTime);
        }

        /**
         * Informs about a malformed url referencing to to script.
         *
         * @param page
         *         the page that causes the problem
         * @param url
         *         the malformed url
         * @param malformedURLException
         *         the occurred exception
         */
        @Override
        @SuppressWarnings("PMD.SystemPrintln")
        public void malformedScriptURL(final HtmlPage page, final String url,
                final MalformedURLException malformedURLException) {
            System.out.println("A JavaScript exception occurred at: " + page.toString()
                    + ", due to the malformed URL: " + url);
            malformedURLException.printStackTrace();
        }

        /**
         * Informs about an exception during load of a javascript file refereed from a page.
         *
         * @param page
         *         the page that causes the problem
         * @param scriptUrl
         *         the url to load the script from
         * @param exception
         *         the occurred exception
         */
        @Override
        @SuppressWarnings("PMD.SystemPrintln")
        public void loadScriptError(final HtmlPage page, final URL scriptUrl, final Exception exception) {
            System.out.println("A JavaScript exception occured at: " + page.toString()
                    + ", while loading the file from the URL: " + scriptUrl.toString());
            exception.printStackTrace();
        }

        @Override
        public void warn(final String message, final String sourceName, final int line, final String lineSource,
                final int lineOffset) {
            // ignore warnings
        }
    }
}
