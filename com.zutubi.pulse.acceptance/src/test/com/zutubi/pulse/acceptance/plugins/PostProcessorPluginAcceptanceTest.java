package com.zutubi.pulse.acceptance.plugins;

import com.zutubi.pulse.acceptance.AcceptanceTestUtils;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.engine.ProjectRecipesConfiguration;
import com.zutubi.pulse.core.engine.marshal.PulseFileLoader;
import com.zutubi.pulse.core.engine.marshal.PulseFileLoaderFactory;
import com.zutubi.pulse.core.marshal.ImportingNotSupportedFileResolver;
import com.zutubi.pulse.core.plugins.Plugin;
import com.zutubi.pulse.core.plugins.PostProcessorExtensionManager;
import static com.zutubi.pulse.core.test.TestUtils.waitForCondition;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.core.tove.config.CoreConfigurationRegistry;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.util.Condition;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.bean.WiringObjectFactory;

import java.io.File;

public class PostProcessorPluginAcceptanceTest extends PulseTestCase
{
    private static final String JAR_NAME = "com.zutubi.pulse.core.postprocessors.test";

    private PostProcessorExtensionManager extensionManager;

    private PulseFileLoaderFactory loaderFactory;
    private WiringObjectFactory objectFactory;
    private File tmpDir;
    private PluginSystem pluginSystem;
    private File samplePostProcessorPlugin;
    private CoreConfigurationRegistry configurationRegistry;
    private TypeRegistry typeRegistry;

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

        typeRegistry = new TypeRegistry();

        configurationRegistry = new CoreConfigurationRegistry();
        configurationRegistry.setTypeRegistry(typeRegistry);
        configurationRegistry.init();

        loaderFactory = new PulseFileLoaderFactory();
        loaderFactory.setTypeRegistry(typeRegistry);
        loaderFactory.setObjectFactory(objectFactory);
        loaderFactory.init();

        extensionManager = new PostProcessorExtensionManager();
        extensionManager.setPluginManager(pluginSystem.getPluginManager());
        extensionManager.setFileLoaderFactory(loaderFactory);
        extensionManager.setConfigurationRegistry(configurationRegistry);
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

    public void testPostProcessorPlugin() throws PulseException, InterruptedException
    {
        // install test plugin.
        Plugin plugin = pluginSystem.install(samplePostProcessorPlugin);
        assertEquals(Plugin.State.ENABLED, plugin.getState());

        // ensure that we are picking up the expected post processors from the installed plugin.
        waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                return extensionManager.getPostProcessor("test.pp") != null;
            }
        }, 30000, "test processor to be ready");

        ProjectRecipesConfiguration prc = new ProjectRecipesConfiguration();

        PulseFileLoader loader = loaderFactory.createLoader();
        loader.load(getInput("xml"), prc, new ImportingNotSupportedFileResolver());
    }
}
