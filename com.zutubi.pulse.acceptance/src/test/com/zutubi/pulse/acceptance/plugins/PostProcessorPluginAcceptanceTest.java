package com.zutubi.pulse.acceptance.plugins;

import com.google.common.io.Files;
import com.zutubi.pulse.acceptance.AcceptanceTestUtils;
import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.core.engine.ProjectRecipesConfiguration;
import com.zutubi.pulse.core.engine.marshal.PulseFileLoader;
import com.zutubi.pulse.core.engine.marshal.PulseFileLoaderFactory;
import com.zutubi.pulse.core.marshal.ImportingNotSupportedFileResolver;
import com.zutubi.pulse.core.plugins.Plugin;
import com.zutubi.pulse.core.plugins.PostProcessorExtensionManager;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.core.tove.config.CoreConfigurationRegistry;
import com.zutubi.tove.type.TypeRegistry;
import com.zutubi.util.Condition;
import com.zutubi.util.RandomUtils;
import com.zutubi.util.bean.WiringObjectFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static com.zutubi.pulse.core.test.TestUtils.waitForCondition;

public class PostProcessorPluginAcceptanceTest extends PulseTestCase
{
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
        tmpDir = createTempDirectory();

        File pkgFile = AcceptanceTestUtils.getPulsePackage();
        if (!pkgFile.exists())
        {
            fail("Pulse package file '" + pkgFile.getAbsolutePath() + "'does not exist.");
        }

        String random = RandomUtils.randomString(10);
        samplePostProcessorPlugin = AcceptanceTestUtils.createTestPlugin(tmpDir, getName() + "." + random, getName() + " " + random);

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

    public void testPostProcessorPlugin() throws PulseException, InterruptedException, IOException
    {
        Plugin plugin = pluginSystem.install(samplePostProcessorPlugin);
        assertEquals(Plugin.State.ENABLED, plugin.getState());

        final String processorTag = plugin.getId() + ".pp";
        // ensure that we are picking up the expected post processors from the installed plugin.
        waitForCondition(new Condition()
        {
            public boolean satisfied()
            {
                return extensionManager.getPostProcessor(processorTag) != null;
            }
        }, 30000, "test processor to be ready");
        
        PulseFileLoader loader = loaderFactory.createLoader();
        loader.load(createPulseFile(processorTag), new ProjectRecipesConfiguration(), new ImportingNotSupportedFileResolver());
    }

    private File createPulseFile(String processorTag) throws IOException
    {
        File pulseFile = copyInputToDirectory("xml", tmpDir);
        String pulseXml = Files.toString(pulseFile, Charset.defaultCharset());
        pulseXml = pulseXml.replaceAll("test\\.pp", processorTag);
        Files.write(pulseXml, pulseFile, Charset.defaultCharset());
        return pulseFile;
    }
}
