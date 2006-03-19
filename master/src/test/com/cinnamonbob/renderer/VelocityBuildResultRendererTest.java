package com.cinnamonbob.renderer;

import com.cinnamonbob.core.model.Changelist;
import com.cinnamonbob.core.model.Revision;
import com.cinnamonbob.core.util.IOUtils;
import com.cinnamonbob.model.BuildResult;
import com.cinnamonbob.model.BuildScmDetails;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.test.BobTestCase;
import org.apache.velocity.app.VelocityEngine;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class VelocityBuildResultRendererTest extends BobTestCase
{
    VelocityBuildResultRenderer renderer;

    protected void setUp() throws Exception
    {
        super.setUp();
        renderer = new VelocityBuildResultRenderer();

        File bobRoot = new File(getBobRoot(), "master/src/templates");

        VelocityEngine engine = new VelocityEngine();
        engine.setProperty("file.resource.loader.path", bobRoot.getAbsolutePath());
//        engine.setProperty("velocimacro.library", "plain-macro.vm");
        engine.init();
        renderer.setVelocityEngine(engine);
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
        BuildResult result = createSuccessfulBuild();

        Revision buildRevision = new Revision();
        buildRevision.setRevisionString("656");

        List<Changelist> changes = new LinkedList<Changelist>();
        Changelist list = new Changelist(new Revision("test author", "short comment", System.currentTimeMillis() - 324252, "655"));
        changes.add(list);
        list = new Changelist(new Revision("author2", "this time we will use a longer comment to make sure that the renderer is applying some sort of trimming to the resulting output", System.currentTimeMillis() - 310000, "656"));
        changes.add(list);

        BuildScmDetails details = new BuildScmDetails(buildRevision, changes);
        result.setScmDetails(details);

        createAndVerify("changes", "another.url", result);
    }

    private BuildResult createSuccessfulBuild()
    {
        BuildResult result = new BuildResult(new Project("test project", "test description"), "test spec", 101);
        result.setId(11);
        result.commence(System.currentTimeMillis() - 10000);
        result.complete();
        return result;
    }

    protected void createAndVerify(String expectedName, String hostUrl, BuildResult result) throws IOException
    {
        // if this lookup returns null, ensure that the txt files are being copied into the classpath as resources.
        InputStream expectedStream = getInput(expectedName, "txt");

        StringWriter writer = new StringWriter();
        renderer.render(hostUrl, result, "plain", writer);
        String got = writer.getBuffer().toString();
        String expected = IOUtils.inputStreamToString(expectedStream);

        assertEquals(expected, got);
    }
}
