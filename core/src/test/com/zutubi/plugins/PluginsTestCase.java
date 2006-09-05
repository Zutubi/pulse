package com.zutubi.plugins;

import com.zutubi.plugins.internal.DefaultComponentDescriptorFactory;
import com.zutubi.plugins.utils.FileUtils;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * <class-comment/>
 */
public abstract class PluginsTestCase extends TestCase
{
    protected DefaultComponentDescriptorFactory descriptorFactory = null;
    protected File pluginA = null;
    protected File pluginB = null;
    protected File pluginC = null;
    protected File pluginD = null;
    protected File pluginX = null;
    protected File pluginY = null;

    public PluginsTestCase()
    {
    }

    public PluginsTestCase(String string)
    {
        super(string);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        descriptorFactory = new DefaultComponentDescriptorFactory();

        pluginA = new File(getPluginsDirectory(), "pluginA.jar");
        pluginB = new File(getPluginsDirectory(), "pluginB.jar");
        pluginC = new File(getPluginsDirectory(), "pluginC.zip");
        pluginD = new File(getPluginsDirectory(), "pluginD");
        pluginX = new File(getPluginsDirectory(), "pluginX.zip");
        pluginY = new File(getPluginsDirectory(), "pluginY.zip");

        assertTrue(pluginA.isFile());
        assertTrue(pluginB.isFile());
        assertTrue(pluginC.isFile());
        assertTrue(pluginD.isDirectory());
        assertTrue(pluginX.isFile());
        assertTrue(pluginY.isFile());
    }

    protected void tearDown() throws Exception
    {
        pluginA = null;
        pluginC = null;
        pluginB = null;
        pluginD = null;
        pluginX = null;
        pluginY = null;

        descriptorFactory = null;

        super.tearDown();
    }

    protected File getPluginsDirectory()
    {
        // another option is to use a system property...

        // if we are running java from the project root directory, this will pick up what we are after.
        File plugins = new File("core/src/test-data/plugins");
        if (plugins.isDirectory())
        {
            return plugins;
        }

        // take a guess at the plugins directory based on the relative location of the compiled class files.
        URL resource = PluginsTestCase.class.getResource("PluginsTestCase.class");
        File root = new File(resource.getPath().replaceFirst("classes/com/zutubi/.*", "")).getParentFile();
        return new File(root, "src/test-data/plugins");
    }

    protected File copyToDir(File source, File dir) throws IOException
    {
        // the destination file is a temporary file within the repository directory.
        if (dir.exists() && !dir.isDirectory())
        {
            throw new IOException();
        }

        if (!dir.exists() && !dir.mkdirs())
        {
            throw new IOException();
        }

        File file = new File(dir, source.getName());
        if (!file.createNewFile())
        {
            throw new IOException();
        }

        FileUtils.copyFile(source, file);
        return dir;
    }
}
