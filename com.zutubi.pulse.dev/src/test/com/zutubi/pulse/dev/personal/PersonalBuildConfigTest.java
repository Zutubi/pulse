package com.zutubi.pulse.dev.personal;

import com.zutubi.pulse.core.scm.api.PersonalBuildUI;
import static com.zutubi.util.CollectionUtils.asPair;
import com.zutubi.util.config.PropertiesConfig;
import static org.mockito.Mockito.*;

import java.io.IOException;

public class PersonalBuildConfigTest extends AbstractPersonalBuildTestCase
{
    public void testPrecedence() throws IOException
    {
        // Should be ui -> base -> base/.. -> user
        createProperties(baseDir, asPair("ui.prop", "base.val"), asPair("base.prop", "base.val"));
        createProperties(baseParentDir, asPair("ui.prop", "baseParent.val"), asPair("base.prop", "baseParent.val"), asPair("baseParent.prop", "baseParent.val"));
        createProperties(userHomeDir, asPair("ui.prop", "user.val"), asPair("base.prop", "user.val"), asPair("baseParent.prop", "user.val"), asPair("user.prop", "user.val"));

        PropertiesConfig uiConfig = new PropertiesConfig();
        uiConfig.setProperty("ui.prop", "ui.val");

        PersonalBuildConfig config = new PersonalBuildConfig(baseDir, uiConfig, mock(PersonalBuildUI.class));
        assertEquals("ui.val", config.getProperty("ui.prop"));
        assertEquals("base.val", config.getProperty("base.prop"));
        assertEquals("baseParent.val", config.getProperty("baseParent.prop"));
        assertEquals("user.val", config.getProperty("user.prop"));
    }

    public void testStore() throws IOException
    {
        PersonalBuildConfig config = new PersonalBuildConfig(baseDir, mock(PersonalBuildUI.class));
        config.setProperty("my.prop", "my.val");
        assertProperties(userHomeDir, asPair("my.prop", "my.val"));
        assertProperties(baseDir);
    }

    public void testStoreLocal() throws IOException
    {
        PersonalBuildConfig config = new PersonalBuildConfig(baseDir, mock(PersonalBuildUI.class));
        config.setProperty("my.prop", "my.val", true);
        assertProperties(userHomeDir);
        assertProperties(baseDir, asPair("my.prop", "my.val"));
    }

    public void testDebugOutput()
    {
        PersonalBuildUI ui = mock(PersonalBuildUI.class);
        stub(ui.isDebugEnabled()).toReturn(true);

        new PersonalBuildConfig(baseDir, ui);
        verify(ui).debug("Assembling configuration...");
        verify(ui).enterContext();
    }
}
