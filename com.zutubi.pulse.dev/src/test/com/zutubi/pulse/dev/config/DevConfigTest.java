package com.zutubi.pulse.dev.config;

import com.zutubi.pulse.core.ui.api.UserInterface;
import com.zutubi.pulse.dev.util.AbstractDevTestCase;
import com.zutubi.util.config.PropertiesConfig;

import java.io.IOException;

import static com.zutubi.util.CollectionUtils.asPair;
import static org.mockito.Mockito.*;

public class DevConfigTest extends AbstractDevTestCase
{
    public void testPrecedence() throws IOException
    {
        // Should be ui -> base -> base/.. -> user
        createProperties(baseDir, asPair("ui.prop", "base.val"), asPair("base.prop", "base.val"));
        createProperties(baseParentDir, asPair("ui.prop", "baseParent.val"), asPair("base.prop", "baseParent.val"), asPair("baseParent.prop", "baseParent.val"));
        createProperties(userHomeDir, asPair("ui.prop", "user.val"), asPair("base.prop", "user.val"), asPair("baseParent.prop", "user.val"), asPair("user.prop", "user.val"));

        PropertiesConfig uiConfig = new PropertiesConfig();
        uiConfig.setProperty("ui.prop", "ui.val");

        DevConfig config = new DevConfig(baseDir, uiConfig, mock(UserInterface.class));
        assertEquals("ui.val", config.getProperty("ui.prop"));
        assertEquals("base.val", config.getProperty("base.prop"));
        assertEquals("baseParent.val", config.getProperty("baseParent.prop"));
        assertEquals("user.val", config.getProperty("user.prop"));
    }

    public void testStore() throws IOException
    {
        DevConfig config = new DevConfig(baseDir, mock(UserInterface.class));
        config.setProperty("my.prop", "my.val");
        assertProperties(userHomeDir, asPair("my.prop", "my.val"));
        assertProperties(baseDir);
    }

    public void testStoreLocal() throws IOException
    {
        DevConfig config = new DevConfig(baseDir, mock(UserInterface.class));
        config.setProperty("my.prop", "my.val", true);
        assertProperties(userHomeDir);
        assertProperties(baseDir, asPair("my.prop", "my.val"));
    }

    public void testDebugOutput()
    {
        UserInterface ui = mock(UserInterface.class);
        stub(ui.isDebugEnabled()).toReturn(true);

        new DevConfig(baseDir, ui);
        verify(ui).debug("Assembling configuration...");
        verify(ui).enterContext();
    }
}
