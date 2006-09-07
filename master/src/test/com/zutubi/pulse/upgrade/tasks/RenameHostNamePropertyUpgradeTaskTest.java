package com.zutubi.pulse.upgrade.tasks;

import com.mockobjects.dynamic.C;
import com.mockobjects.dynamic.Mock;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.bootstrap.MasterConfigurationSupport;
import com.zutubi.pulse.config.PropertiesConfig;
import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.upgrade.UpgradeException;

import java.util.Properties;

/**
 * <class-comment/>
 */
public class RenameHostNamePropertyUpgradeTaskTest extends PulseTestCase
{
    public RenameHostNamePropertyUpgradeTaskTest()
    {
    }

    public RenameHostNamePropertyUpgradeTaskTest(String name)
    {
        super(name);
    }

    public void testPropertyUpgrade() throws UpgradeException
    {
        Properties props = new Properties();
        props.put("host.name", "speedy");

        upgradeProperties(props);

        assertEquals("http://speedy", props.getProperty("webapp.base.url"));
        assertFalse(props.containsKey("host.name"));
    }

    public void testPropertyUpgradeRemovesTrailingSlash() throws UpgradeException
    {
        Properties props = new Properties();
        props.put("host.name", "speedy:8080/");

        upgradeProperties(props);

        assertEquals("http://speedy:8080", props.getProperty("webapp.base.url"));
        assertFalse(props.containsKey("host.name"));
    }

    private MasterConfigurationSupport upgradeProperties(Properties props) throws UpgradeException
    {
        MasterConfigurationSupport confSupport = new MasterConfigurationSupport(new PropertiesConfig(props));

        Mock mockConfigurationManager = new Mock(MasterConfigurationManager.class);
        mockConfigurationManager.matchAndReturn("getAppConfig", C.ANY_ARGS, confSupport);

        RenameHostNamePropertyUpgradeTask upgradeTask = new RenameHostNamePropertyUpgradeTask();
        upgradeTask.setConfigurationManager((MasterConfigurationManager) mockConfigurationManager.proxy());

        upgradeTask.execute(new MockUpgradeContext());
        return confSupport;
    }


}
