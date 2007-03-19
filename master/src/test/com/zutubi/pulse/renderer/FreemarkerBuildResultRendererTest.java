package com.zutubi.pulse.renderer;

import com.mockobjects.dynamic.Mock;
import com.zutubi.pulse.committransformers.CommitMessageTransformerManager;
import com.zutubi.pulse.committransformers.LinkCommitMessageTransformer;
import com.zutubi.pulse.core.model.*;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.util.Constants;
import com.zutubi.pulse.util.IOUtils;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;

/**
 */
public class FreemarkerBuildResultRendererTest extends PulseTestCase
{
    private boolean generate = false;

    private FreemarkerBuildResultRenderer renderer;

    protected void setUp() throws Exception
    {
        super.setUp();
        renderer = new FreemarkerBuildResultRenderer();
        File pulseRoot = new File(getPulseRoot(), "master/src/templates");

        Configuration freemarkerConfiguration = new Configuration();
        freemarkerConfiguration.setDirectoryForTemplateLoading(pulseRoot);
        freemarkerConfiguration.setObjectWrapper(new DefaultObjectWrapper());
        freemarkerConfiguration.addAutoInclude("macro.ftl");
        renderer.setFreemarkerConfiguration(freemarkerConfiguration);

        LinkCommitMessageTransformer tx = new LinkCommitMessageTransformer("Jira");
        tx.setExpression("(CIB-[0-9]+)");
        tx.setLink("http://jira.zutubi.com/browse/$0");

        List<CommitMessageTransformer> txs = new ArrayList<CommitMessageTransformer>(1);
        txs.add(tx);

        Mock commitMessageTransformerManager = new Mock(CommitMessageTransformerManager.class);
        renderer.setCommitMessageTransformerManager((CommitMessageTransformerManager) commitMessageTransformerManager.proxy());
        commitMessageTransformerManager.matchAndReturn("getCommitMessageTransformers", txs);
    }

    protected void tearDown() throws Exception
    {
        renderer = null;
        super.tearDown();
    }

    public void testBasicSuccess() throws Exception
    {
        BuildResult result = createSuccessfulBuild();
        createAndVerify("basic", "http://test.url:8080", result, new LinkedList<Changelist>());
    }

    public void testWithChanges() throws Exception
    {
        List<Changelist> changes = getChanges();
        BuildResult result = createBuildWithChanges(changes);

        createAndVerify("changes", "http://another.url", result, changes);
    }

    public void testWithErrors() throws Exception
    {
        errorsHelper("plain-text-email");
    }

    public void testSimpleInstantBasic() throws Exception
    {
        BuildResult result = createSuccessfulBuild();
        createAndVerify("basic", "simple-instant-message", "http://test.url:8080", result, new LinkedList<Changelist>());
    }

    public void testSimpleInstantError() throws Exception
    {
        errorsHelper("simple-instant-message");
    }

    public void testDetailedInstantBasic() throws Exception
    {
        BuildResult result = createSuccessfulBuild();
        createAndVerify("basic", "detailed-instant-message", "http://test.url:8080", result, new LinkedList<Changelist>());
    }

    public void testDetailedInstantError() throws Exception
    {
        errorsHelper("detailed-instant-message");
    }

    public void testDetailedInstantSingleStageFailure() throws Exception
    {
        singleStageFailureHelper("detailed-instant-message");
    }

    public void testDetailedInstantFailures() throws Exception
    {
        failuresHelper("detailed-instant-message", false);
    }

    public void testHTMLWithChanges() throws Exception
    {
        List<Changelist> changes = getChanges();
        BuildResult result = createBuildWithChanges(changes);

        createAndVerify("changes", "html-email", "http://another.url", result, changes);
    }

    public void testHTMLWithErrors() throws Exception
    {
        errorsHelper("html-email");
    }

    public void testWithFailures() throws Exception
    {
        failuresHelper("plain-text-email", false);
    }

    public void testHTMLWithFailures() throws Exception
    {
        failuresHelper("html-email", false);
    }

    public void testWithSingleStageFailure() throws Exception
    {
        singleStageFailureHelper("plain-text-email");
    }

    public void testHTMLWithSingleStageFailure() throws Exception
    {
        singleStageFailureHelper("html-email");
    }

    public void testWithExcessFailures() throws Exception
    {
        failuresHelper("plain-text-email", true);
    }

    public void testHTMLWithExcessFailures() throws Exception
    {
        failuresHelper("html-email", true);
    }

    public void testPersonalSimpleInstant() throws Exception
    {
        personalBuildHelper("simple-instant-message");
    }

    public void testPersonalDetailedInstant() throws Exception
    {
        personalBuildHelper("detailed-instant-message");
    }

    public void testPersonalPlainTextEmail() throws Exception
    {
        personalBuildHelper("plain-text-email");
    }

    public void testPersonalHTMLEmail() throws Exception
    {
        personalBuildHelper("html-email");
    }

    public void testProjectOverviewSuccess() throws Exception
    {
        BuildResult result = createSuccessfulBuild();
        createAndVerify("basic", "html-project-overview", "http://test.url:8080", result, new LinkedList<Changelist>());
    }

    public void testProjectOverviewFailureNoPreviousSuccess() throws Exception
    {
        BuildResult result = createSuccessfulBuild();
        result.failure("i failed");
        createAndVerify("failednosuccess", "html-project-overview", "http://test.url:8080", result, new LinkedList<Changelist>(), null, 0, 0);
    }

    public void testProjectOverviewFailurePreviousSuccess() throws Exception
    {
        BuildResult previous = new BuildResult(new TriggerBuildReason("scm trigger"), new Project("test project", "test description"), new BuildSpecification("test spec"), 90, false);
        initialiseResult(previous);
        previous.getStamps().setStartTime(System.currentTimeMillis() - Constants.DAY * 3);
        BuildResult result = createSuccessfulBuild();
        result.failure("i failed");
        createAndVerify("failedsuccess", "html-project-overview", "http://test.url:8080", result, new LinkedList<Changelist>(), previous, 33, 10);
    }

    private void errorsHelper(String type) throws Exception
    {
        List<Changelist> changes = getChanges();
        BuildResult result = createBuildWithChanges(changes);
        result.error("test error message");
        result.addFeature(Feature.Level.WARNING, "warning message on result");
        RecipeResultNode firstNode = result.getRoot().getChildren().get(0);
        firstNode.getResult().error("test recipe error message");

        RecipeResultNode nestedNode = firstNode.getChildren().get(0);
        nestedNode.getResult().failure("test recipe failure message with the unfortunate need to wrap because it is really quite ridiculously long");

        RecipeResultNode secondNode = result.getRoot().getChildren().get(1);
        RecipeResult secondResult = secondNode.getResult();

        CommandResult command = new CommandResult("test command");
        command.error("bad stuff happened, so wrap this: 000000000000000000000000000000000000000000000000000000000000000000000");
        secondResult.add(command);

        command = new CommandResult("artifact command");
        command.failure("artifacts let me down");

        StoredFileArtifact artifact = new StoredFileArtifact("first-artifact/testpath");
        artifact.addFeature(new Feature(Feature.Level.INFO, "info message"));
        artifact.addFeature(new Feature(Feature.Level.ERROR, "error message"));
        artifact.addFeature(new PlainFeature(Feature.Level.WARNING, "warning message", 19));
        command.addArtifact(new StoredArtifact("first-artifact", artifact));

        artifact = new StoredFileArtifact("second-artifact/this/time/a/very/very/very/very/long/pathname/which/will/look/ugly/i/have/no/doubt");
        artifact.addFeature(new PlainFeature(Feature.Level.ERROR, "error 1", 1000000));
        artifact.addFeature(new Feature(Feature.Level.ERROR, "error 2"));
        artifact.addFeature(new Feature(Feature.Level.ERROR, "error 3: in this case a longer error message so i can see how the wrapping works on the artifact messages"));
        command.addArtifact(new StoredArtifact("second-artifact", artifact));

        secondResult.add(command);

        createAndVerify("errors", type, "http://another.url", result, changes);
    }

    private void failuresHelper(String type, boolean excessFailures) throws Exception
    {
        BuildResult result = createSuccessfulBuild();
        result.failure("test failed tests");

        RecipeResultNode firstNode = result.getRoot().getChildren().get(0);
        firstNode.getResult().failure("tests failed dude");

        RecipeResultNode nestedNode = firstNode.getChildren().get(0);
        nestedNode.getResult().failure("tests failed nested dude");

        RecipeResultNode secondNode = result.getRoot().getChildren().get(1);
        RecipeResult secondResult = secondNode.getResult();

        CommandResult command = new CommandResult("failing tests");
        command.failure("tests let me down");

        TestSuiteResult tests = new TestSuiteResult();

        StoredFileArtifact artifact = new StoredFileArtifact("first-artifact/testpath");
        TestSuiteResult rootSuite = new TestSuiteResult("root test suite");
        rootSuite.add(new TestCaseResult("2 failed", 0, TestCaseResult.Status.FAILURE, "a failure message which is bound to be detailed, potentially to the extreme but in this case just to wrap a bit"));
        rootSuite.add(new TestCaseResult("3 error", 0, TestCaseResult.Status.ERROR, "short error"));

        TestSuiteResult nestedSuite = new TestSuiteResult("nested suite");
        nestedSuite.add(new TestCaseResult("n1 failed", 0, TestCaseResult.Status.FAILURE, "a failure message which is bound to be detailed, potentially to the extreme but in this case just to wrap a bit"));
        nestedSuite.add(new TestCaseResult("n2 error", 0, TestCaseResult.Status.ERROR, "short error"));
        rootSuite.add(nestedSuite);

        tests.add(rootSuite);
        command.addArtifact(new StoredArtifact("first-artifact", artifact));

        artifact = new StoredFileArtifact("second-artifact/this/time/a/very/very/very/very/long/pathname/which/will/look/ugly/i/have/no/doubt");
        tests.add(new TestCaseResult("test case at top level", 0, TestCaseResult.Status.FAILURE, "and i failed"));
        command.addArtifact(new StoredArtifact("second-artifact", artifact));

        secondResult.add(command);
        secondResult.setFailedTestResults(tests);
        TestResultSummary summary = tests.getSummary();
        if(excessFailures)
        {
            summary.setFailures(summary.getFailures() + 123);
        }

        secondResult.setTestSummary(summary);
        createAndVerify((excessFailures ? "excess" : "") + "failures", type, "http://host.url", result);
    }

    private void singleStageFailureHelper(String type) throws Exception
    {
        BuildResult result = createSuccessfulBuild();
        result.failure("a stage failed");

        RecipeResultNode secondNode = result.getRoot().getChildren().get(1);
        RecipeResult secondResult = secondNode.getResult();

        CommandResult command = new CommandResult("failing tests");
        command.failure("i failed");
        secondResult.add(command);
        createAndVerify("singelstagefailure", type, "http://host.url", result);
    }

    private void personalBuildHelper(String type) throws Exception
    {
        User user = new User("jason", "Jason Sankey");
        BuildResult result = new BuildResult(user, new Project("my project", "project description"), new BuildSpecification("nightly"), 12);
        initialiseResult(result);

        result.failure("test failed tests");

        RecipeResultNode firstNode = result.getRoot().getChildren().get(0);
        firstNode.getResult().failure("tests failed dude");

        RecipeResultNode nestedNode = firstNode.getChildren().get(0);
        nestedNode.getResult().failure("tests failed nested dude");

        RecipeResultNode secondNode = result.getRoot().getChildren().get(1);
        RecipeResult secondResult = secondNode.getResult();

        CommandResult command = new CommandResult("failing tests");
        command.failure("tests let me down");

        TestSuiteResult tests = new TestSuiteResult();

        StoredFileArtifact artifact = new StoredFileArtifact("first-artifact/testpath");
        TestSuiteResult rootSuite = new TestSuiteResult("root test suite");
        rootSuite.add(new TestCaseResult("2 failed", 0, TestCaseResult.Status.FAILURE, "a failure message which is bound to be detailed, potentially to the extreme but in this case just to wrap a bit"));
        rootSuite.add(new TestCaseResult("3 error", 0, TestCaseResult.Status.ERROR, "short error"));

        TestSuiteResult nestedSuite = new TestSuiteResult("nested suite");
        nestedSuite.add(new TestCaseResult("n1 failed", 0, TestCaseResult.Status.FAILURE, "a failure message which is bound to be detailed, potentially to the extreme but in this case just to wrap a bit"));
        nestedSuite.add(new TestCaseResult("n2 error", 0, TestCaseResult.Status.ERROR, "short error"));
        rootSuite.add(nestedSuite);

        tests.add(rootSuite);
        command.addArtifact(new StoredArtifact("first-artifact", artifact));

        artifact = new StoredFileArtifact("second-artifact/this/time/a/very/very/very/very/long/pathname/which/will/look/ugly/i/have/no/doubt");
        tests.add(new TestCaseResult("test case at top level", 0, TestCaseResult.Status.FAILURE, "and i failed"));
        command.addArtifact(new StoredArtifact("second-artifact", artifact));

        secondResult.add(command);
        secondResult.setFailedTestResults(tests);
        TestResultSummary summary = tests.getSummary();
        secondResult.setTestSummary(summary);
        createAndVerify("personal", type, "http://host.url", result);
    }

    private BuildResult createBuildWithChanges(List<Changelist> changes)
    {
        BuildResult result = createSuccessfulBuild();

        Revision buildRevision = new Revision();
        buildRevision.setRevisionString("656");

        BuildScmDetails details = new BuildScmDetails(buildRevision);
        result.setScmDetails(details);

        for(Changelist change: changes)
        {
            change.addResultId(result.getId());
        }

        return result;
    }

    private List<Changelist> getChanges()
    {
        List<Changelist> changes = new LinkedList<Changelist>();
        Changelist list = new Changelist("scm", new Revision("test author", "CIB-1: short comment", 324252, "655"));
        changes.add(list);
        list = new Changelist("scm", new Revision("author2", "this time we will use a longer comment to make sure that the renderer is applying some sort of trimming to the resulting output dadada da dadad ad ad adadad ad ad ada d adada dad ad ad d ad ada da d", 310000, "656"));
        changes.add(list);
        return changes;
    }

    private BuildResult createSuccessfulBuild()
    {
        BuildResult result = new BuildResult(new TriggerBuildReason("scm trigger"), new Project("test project", "test description"), new BuildSpecification("test spec"), 101, false);
        initialiseResult(result);
        return result;
    }

    private void initialiseResult(BuildResult result)
    {
        result.setId(11);
        result.setScmDetails(new BuildScmDetails());
        result.commence(10000);

        RecipeResult recipeResult = new RecipeResult("first recipe");
        recipeResult.commence();
        recipeResult.complete();
        RecipeResultNode node = new RecipeResultNode(new PersistentName("first stage"), recipeResult);
        result.getRoot().addChild(node);

        recipeResult = new RecipeResult("second recipe");
        recipeResult.commence();
        recipeResult.complete();
        node = new RecipeResultNode(new PersistentName("second stage"), recipeResult);
        result.getRoot().addChild(node);

        recipeResult = new RecipeResult("nested recipe");
        recipeResult.commence();
        recipeResult.complete();
        node = new RecipeResultNode(new PersistentName("nested stage"), recipeResult);
        result.getRoot().getChildren().get(0).addChild(node);

        result.complete();
        result.getStamps().setEndTime(100000);
    }

    protected void createAndVerify(String expectedName, String baseUrl, BuildResult result, List<Changelist> changelists) throws IOException
    {
        createAndVerify(expectedName, "plain-text-email", baseUrl, result, changelists);
    }

    protected void createAndVerify(String expectedName, String type, String baseUrl, BuildResult result) throws IOException, URISyntaxException
    {
        createAndVerify(expectedName, type, baseUrl, result, new LinkedList<Changelist>());
    }

    protected void createAndVerify(String expectedName, String type, String baseUrl, BuildResult result, List<Changelist> changelists) throws IOException
    {
        createAndVerify(expectedName, type, baseUrl, result, changelists, null, 0, 0);
    }

    protected void createAndVerify(String expectedName, String type, String baseUrl, BuildResult result, List<Changelist> changelists, BuildResult lastSuccess, int unsuccessfulBuilds, int unsuccessfulDays) throws IOException
    {
        Map<String, Object> dataMap = getDataMap(baseUrl, result, changelists, lastSuccess, unsuccessfulBuilds, unsuccessfulDays);

        String extension = "txt";

        // Just a hack that makes it easier to view expected output in a
        // browser during development.
        if (type.equals("html-email"))
        {
            extension = "html";
        }
        else if(type.equals("html-project-overview"))
        {
            extension = "overview.html";
        }
        else if(type.equals("simple-instant-message"))
        {
            extension = "si.txt";
        }
        else if(type.equals("detailed-instant-message"))
        {
            extension = "di.txt";
        }

        if (generate)
        {
            File expected = getTestDataFile("master", expectedName, extension);
            OutputStream outStream = null;
            Writer writer = null;

            try
            {
                outStream = new FileOutputStream(expected);
                writer = new OutputStreamWriter(outStream);
                renderer.render(result, dataMap, type, writer);
            }
            finally
            {
                IOUtils.close(outStream);
                IOUtils.close(writer);
            }
        }
        else
        {
            InputStream expectedStream = null;

            try
            {
                expectedStream = getInput(expectedName, extension);

                StringWriter writer = new StringWriter();
                renderer.render(result, dataMap, type, writer);
                String got = replaceTimestamps(writer.getBuffer().toString());
                String expected = replaceTimestamps(IOUtils.inputStreamToString(expectedStream));
                assertEquals(expected, got);
            }
            finally
            {
                IOUtils.close(expectedStream);
            }
        }
    }

    private Map<String, Object> getDataMap(String baseUrl, BuildResult result, List<Changelist> changelists)
    {
        return getDataMap(baseUrl, result, changelists, null, 0, 0);
    }

    private Map<String, Object> getDataMap(String baseUrl, BuildResult result, List<Changelist> changelists, BuildResult lastSuccess, int unsuccessfulBuilds, int unsuccessfulDays)
    {

        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("renderer", renderer);
        dataMap.put("baseUrl", baseUrl);
        dataMap.put("project", result.getProject());
        dataMap.put("status", result.succeeded() ? "healthy" : "broken");
        dataMap.put("result", result);
        dataMap.put("model", result);
        dataMap.put("changelists", changelists);
        dataMap.put("errorLevel", Feature.Level.ERROR);
        dataMap.put("warningLevel", Feature.Level.WARNING);

        if(lastSuccess != null)
        {
            dataMap.put("lastSuccess", lastSuccess);
            dataMap.put("unsuccessfulBuilds", unsuccessfulBuilds);
            dataMap.put("unsuccessfulDays", unsuccessfulDays);
        }
        
        return dataMap;
    }

    private String replaceTimestamps(String str)
    {
        return str.replaceAll("\n.*ago<", "@@@@").replaceAll("\n[0-9]+ ms", "@@@@");
    }
}
