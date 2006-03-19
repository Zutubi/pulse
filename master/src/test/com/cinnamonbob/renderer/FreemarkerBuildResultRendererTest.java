package com.cinnamonbob.renderer;

import com.cinnamonbob.core.model.*;
import com.cinnamonbob.core.util.IOUtils;
import com.cinnamonbob.model.BuildResult;
import com.cinnamonbob.model.BuildScmDetails;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.RecipeResultNode;
import com.cinnamonbob.test.BobTestCase;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class FreemarkerBuildResultRendererTest extends BobTestCase
{
    FreemarkerBuildResultRenderer renderer;

    protected void setUp() throws Exception
    {
        super.setUp();
        renderer = new FreemarkerBuildResultRenderer();

        File bobRoot = new File(getBobRoot(), "master/src/templates");

        Configuration freemarkerConfiguration = new Configuration();
        freemarkerConfiguration.setDirectoryForTemplateLoading(bobRoot);
        freemarkerConfiguration.setObjectWrapper(new DefaultObjectWrapper());
        freemarkerConfiguration.addAutoInclude("macro.ftl");
        renderer.setFreemarkerConfiguration(freemarkerConfiguration);
    }

    protected void tearDown() throws Exception
    {
        renderer = null;
        super.tearDown();
    }

    public void testBasicSuccess() throws IOException
    {
        BuildResult result = createSuccessfulBuild();
        createAndVerify("basic", "test.url:8080", result);
    }

    public void testWithChanges() throws IOException
    {
        BuildResult result = createBuildWithChanges();

        createAndVerify("changes", "another.url", result);
    }

    public void testWithErrors() throws IOException
    {
        BuildResult result = createBuildWithChanges();
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
        artifact.addFeature(new Feature(Feature.Level.WARNING, "warning message"));
        command.addArtifact(new StoredArtifact("first-artifact", artifact));

        artifact = new StoredFileArtifact("second-artifact/this/time/a/very/very/very/very/long/pathname/which/will/look/ugly/i/have/no/doubt");
        artifact.addFeature(new Feature(Feature.Level.ERROR, "error 1"));
        artifact.addFeature(new Feature(Feature.Level.ERROR, "error 2"));
        artifact.addFeature(new Feature(Feature.Level.ERROR, "error 3: in this case a longer error message so i can see how the wrapping works on the artifact messages"));
        command.addArtifact(new StoredArtifact("second-artifact", artifact));

        secondResult.add(command);

        createAndVerify("errors", "another.url", result);
    }

    private BuildResult createBuildWithChanges()
    {
        BuildResult result = createSuccessfulBuild();

        Revision buildRevision = new Revision();
        buildRevision.setRevisionString("656");

        List<Changelist> changes = new LinkedList<Changelist>();
        Changelist list = new Changelist(new Revision("test author", "short comment", System.currentTimeMillis() - 324252, "655"));
        changes.add(list);
        list = new Changelist(new Revision("author2", "this time we will use a longer comment to make sure that the renderer is applying some sort of trimming to the resulting output dadada da dadad ad ad adadad ad ad ada d adada dad ad ad d ad ada da d", System.currentTimeMillis() - 310000, "656"));
        changes.add(list);

        BuildScmDetails details = new BuildScmDetails(buildRevision, changes);
        result.setScmDetails(details);
        return result;
    }

    private BuildResult createSuccessfulBuild()
    {
        BuildResult result = new BuildResult(new Project("test project", "test description"), "test spec", 101);
        result.setId(11);
        result.setScmDetails(new BuildScmDetails());
        result.commence(System.currentTimeMillis() - 10000);

        RecipeResult recipeResult = new RecipeResult("first recipe");
        RecipeResultNode node = new RecipeResultNode(recipeResult);
        result.getRoot().addChild(node);

        recipeResult = new RecipeResult("second recipe");
        node = new RecipeResultNode(recipeResult);
        result.getRoot().addChild(node);

        recipeResult = new RecipeResult("nested recipe");
        node = new RecipeResultNode(recipeResult);
        result.getRoot().getChildren().get(0).addChild(node);

        result.complete();
        return result;
    }

    protected void createAndVerify(String expectedName, String hostUrl, BuildResult result) throws IOException
    {
        InputStream expectedStream = getInput(expectedName, "txt");

        StringWriter writer = new StringWriter();
        renderer.render(hostUrl, result, "plain", writer);
        String got = writer.getBuffer().toString();
        String expected = IOUtils.inputStreamToString(expectedStream);

        assertEquals(expected, got);
    }
}
