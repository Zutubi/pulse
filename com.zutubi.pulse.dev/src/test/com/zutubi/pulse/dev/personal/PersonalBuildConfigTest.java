package com.zutubi.pulse.dev.personal;

import static com.zutubi.util.CollectionUtils.asPair;
import com.zutubi.util.config.PropertiesConfig;

import java.io.IOException;

public class PersonalBuildConfigTest extends AbstractPersonalBuildTestCase
{
    public void testPrecedence() throws IOException
    {
        // Should be ui -> base -> base/.. -> user
        createProperties(baseDir, asPair("ui.prop", "base.val"), asPair("base.prop", "base.val"));
        createProperties(baseParentDir, asPair("ui.prop", "baseParent.val"), asPair("base.prop", "baseParent.val"), asPair("baseParent.prop", "baseParent.val"));
        createProperties(userHomeDir, asPair("ui.prop", "user.val"), asPair("base.prop", "user.val"), asPair("baseParent.prop", "user.val"), asPair("user.prop", "user.val"));

        PropertiesConfig ui = new PropertiesConfig();
        ui.setProperty("ui.prop", "ui.val");

        PersonalBuildConfig config = new PersonalBuildConfig(baseDir, ui);
        assertEquals("ui.val", config.getProperty("ui.prop"));
        assertEquals("base.val", config.getProperty("base.prop"));
        assertEquals("baseParent.val", config.getProperty("baseParent.prop"));
        assertEquals("user.val", config.getProperty("user.prop"));
    }

    public void testStore() throws IOException
    {
        PersonalBuildConfig config = new PersonalBuildConfig(baseDir);
        config.setProperty("my.prop", "my.val");
        assertProperties(userHomeDir, asPair("my.prop", "my.val"));
        assertProperties(baseDir);
    }

    public void testStoreLocal() throws IOException
    {
        PersonalBuildConfig config = new PersonalBuildConfig(baseDir);
        config.setProperty("my.prop", "my.val", true);
        assertProperties(userHomeDir);
        assertProperties(baseDir, asPair("my.prop", "my.val"));
    }
}
