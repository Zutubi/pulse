package com.zutubi.pulse.acceptance.plugins;

import com.zutubi.pulse.acceptance.AcceptanceTestUtils;
import com.zutubi.pulse.core.ImportingNotSupportedFileResolver;
import com.zutubi.pulse.core.PulseFile;
import com.zutubi.pulse.core.PulseFileLoader;
import com.zutubi.pulse.core.PulseFileLoaderFactory;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.engine.api.Reference;
import com.zutubi.pulse.core.plugins.Plugin;
import com.zutubi.pulse.core.plugins.PostProcessorExtensionManager;
import static com.zutubi.pulse.core.test.TestUtils.waitForCondition;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.util.Condition;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.bean.WiringObjectFactory;

import java.io.File;

public class PostProcessorPluginAcceptanceTest extends PulseTestCase
{
    private static final String JAR_NAME = "com.zutubi.bundles.postprocessor.sample_1.0.0";

    private PostProcessorExtensionManager extensionManager;

    private PulseFileLoaderFactory loaderFactory;
    private WiringObjectFactory objectFactory;
    private File tmpDir;
    private PluginSystem pluginSystem;
    private File samplePostProcessorPlugin;

    protected void setUp() throws Exception
    {
        super.setUp();

        // base directory will be cleaned up at the end of the test.
        tmpDir = FileSystemUtils.createTempDir();

        File pkgFile = AcceptanceTestUtils.getPulsePackage();
        if (!pkgFile.exists())
        {
            fail("Pulse package file '" + pkgFile.getAbsolutePath() + "'does not exist.");
        }

        samplePostProcessorPlugin = copyInputToDirectory(JAR_NAME, "jar", tmpDir);

        pluginSystem = new PluginSystem(pkgFile, tmpDir);
        pluginSystem.startup();

        objectFactory = new WiringObjectFactory();

        loaderFactory = new PulseFileLoaderFactory();
        loaderFactory.setObjectFactory(objectFactory);

        extensionManager = new PostProcessorExtensionManager();
        extensionManager.setPluginManager(pluginSystem.getPluginManager());
        extensionManager.setFileLoaderFactory(loaderFactory);
        extensionManager.init();
        extensionManager.initialiseExtensions();

        objectFactory.initProperties(this);
    }

    protected void tearDown() throws Exception
    {
        pluginSystem.shutdown();
        removeDirectory(tmpDir);

        super.tearDown();
    }

    public void testPostProcessorPlugins() throws PulseException, InterruptedException
    {
        // install test plugin.
        Plugin plugin = pluginSystem.install(samplePostProcessorPlugin);
        assertEquals(Plugin.State.ENABLED, plugin.getState());

        // ensure that we are picking up the expected post processors from the installed plugin.
        waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                return extensionManager.getPostProcessor("sample.pp") != null;
            }
        }, 30000, "sample processor to be ready");

        PulseFile pf = new PulseFile();

        PulseFileLoader loader = loaderFactory.createLoader();
        loader.load(getInput("testPostProcessorPlugin", "xml"), pf, new ImportingNotSupportedFileResolver());

        Reference ref = pf.getReference("sample.pp");

        // verify that the reference is to the expected instance.
        assertNotNull(ref.getValue());
    }
}
