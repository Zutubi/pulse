package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.pulse.core.test.PulseTestCase;
import com.zutubi.pulse.master.PostProcessorManager;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import org.apache.velocity.app.VelocityEngine;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

public abstract class TemplateTypeConfigurationTestBase extends PulseTestCase
{
    protected File tmpDir;
    protected VelocityEngine engine;
    protected boolean generateMode = false;

    protected void setUp() throws Exception
    {
        super.setUp();
        engine = new VelocityEngine();
        File pulseRoot = new File(getPulseRoot(), "com.zutubi.pulse.master/src/templates");
        engine.setProperty("file.resource.loader.path", pulseRoot.getAbsolutePath());
        engine.init();
        TemplateTypeConfiguration type = getType();
        type.setVelocityEngine(engine);
        type.setPostProcessorManager(new PostProcessorManager()
        {
            public PostProcessorFragment getProcessor(String name)
            {
                return new DefaultPostProcessorFragment(name, name);
            }

            public Map<String, PostProcessorFragment> getAvailableProcessors()
            {
                throw new RuntimeException("Method not yet implemented");
            }
        });
        tmpDir = FileSystemUtils.createTempDir(getClass().getName(), "");
    }

    protected void tearDown() throws Exception
    {
        FileSystemUtils.rmdir(tmpDir);
        super.tearDown();
    }

    protected void createAndVerify(String expectedName) throws Exception
    {
        String got = getType().getPulseFile(null, null, null);
        File file = new File(getPulseRoot(), FileSystemUtils.composeFilename("com.zutubi.pulse.master", "src", "test", "com", "zutubi", "pulse", "master", "tove", "config", "project", "types", getClass().getSimpleName() + "." + expectedName + ".xml"));

        if(generateMode)
        {
            FileSystemUtils.createFile(file, got);
        }
        else
        {
            InputStream expectedStream = null;

            try
            {
                expectedStream = new FileInputStream(file);
                String expected = IOUtils.inputStreamToString(expectedStream);
                assertEquals(expected, got);
                expectedStream.close();
            }
            finally
            {
                IOUtils.close(expectedStream);
            }
        }

        // Ensure syntactic correctness
        // FIXME: cannot do this part as we do not have the command types in
        // our classpath.  The real solution to this is to move these tests
        // into the plugins where they belong.
//        PulseFileLoaderFactory fileLoaderFactory = new PulseFileLoaderFactory();
//        fileLoaderFactory.setObjectFactory(new DefaultObjectFactory());
//        PulseFileLoader loader = fileLoaderFactory.createLoader();
//
//        Scope scope = new Scope();
//        scope.add(new Property("base.dir", "testbase"));
//        FileInputStream input = null;
//
//        try
//        {
//            input = new FileInputStream(file);
//            loader.load(input, new PulseFile(), scope, new FileResourceRepository(), null);
//        }
//        finally
//        {
//            IOUtils.close(input);
//        }
    }

    public abstract TemplateTypeConfiguration getType();

    protected void addArtifacts(TemplateTypeConfiguration typeConfiguration)
    {
        FileArtifactConfiguration file = new FileArtifactConfiguration("artifact 1", "filename");
        file.addPostprocessor("junit");
        typeConfiguration.addArtifact(file);

        file = new FileArtifactConfiguration("artifact 2", "filename");
        file.addPostprocessor("junit");
        typeConfiguration.addArtifact(file);
    }
}
