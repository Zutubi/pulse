package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.master.bootstrap.SimpleMasterConfigurationManager;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.util.Hashtable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;

public class LoggingAcceptanceTest extends BaseXmlRpcAcceptanceTest
{
    private static final String PROPERTY_LOGGING = "logging";
    private static final String PROPERTY_EVENT_LOGGING_ENABLED = "eventLoggingEnabled";
    private static final String PROPERTY_CONFIG_LOGGING_ENABLED = "configAuditLoggingEnabled";
    private static final String PROPERTY_SCM_POLLING_INTERVAL = "scmPollingInterval";

    private static final String PATH_LOGGING_CONFIG = PathUtils.getPath(GlobalConfiguration.SCOPE_NAME, PROPERTY_LOGGING);

    private File logDir;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        loginAsAdmin();
        disableExtraLogging();
        logDir = new File(xmlRpcHelper.getServerInfo().get(SimpleMasterConfigurationManager.CORE_PROPERTY_PULSE_LOG_DIR));
    }

    @Override
    protected void tearDown() throws Exception
    {
        disableExtraLogging();
        logout();
        super.tearDown();
    }

    private void disableExtraLogging() throws Exception
    {
        setEventLoggingEnabled(false);
        setConfigLoggingEnabled(false);
    }

    public void testEnableEventLogging() throws Exception
    {
        File firstEventLog = new File(logDir, "event0.0.log");
        long originalLength = firstEventLog.length();

        // A config change will set off some events.
        makeConfigChange();

        assertEquals(originalLength, firstEventLog.length());

        setEventLoggingEnabled(true);
        makeConfigChange();

        assertThat(firstEventLog.length(), greaterThan(originalLength));
        String content = IOUtils.fileToString(firstEventLog);
        assertThat(content, containsString("Save Event: " + GlobalConfiguration.SCOPE_NAME));
    }

    public void testEnableConfigLogging() throws Exception
    {
        File firstConfigLog = new File(logDir, "config0.0.log");
        long originalLength = firstConfigLog.length();

        makeConfigChange();

        assertEquals(originalLength, firstConfigLog.length());

        setConfigLoggingEnabled(true);
        makeConfigChange();

        assertThat(firstConfigLog.length(), greaterThan(originalLength));
        String content = IOUtils.fileToString(firstConfigLog);
        assertThat(content, containsString("admin: updated: " + GlobalConfiguration.SCOPE_NAME));
    }

    private void makeConfigChange() throws Exception
    {
        Hashtable<String, Object> globalConfig = xmlRpcHelper.getConfig(GlobalConfiguration.SCOPE_NAME);
        Integer current = (Integer) globalConfig.get(PROPERTY_SCM_POLLING_INTERVAL);
        globalConfig.put(PROPERTY_SCM_POLLING_INTERVAL, current + 1);
        xmlRpcHelper.saveConfig(GlobalConfiguration.SCOPE_NAME, globalConfig, false);
    }

    private void setEventLoggingEnabled(boolean enabled) throws Exception
    {
        updateLoggingConfig(PROPERTY_EVENT_LOGGING_ENABLED, enabled);
    }

    private void setConfigLoggingEnabled(boolean enabled) throws Exception
    {
        updateLoggingConfig(PROPERTY_CONFIG_LOGGING_ENABLED, enabled);
    }

    private void updateLoggingConfig(String property, boolean value) throws Exception
    {
        Hashtable<String, Object> loggingConfig = xmlRpcHelper.getConfig(PATH_LOGGING_CONFIG);
        loggingConfig.put(property, value);
        xmlRpcHelper.saveConfig(PATH_LOGGING_CONFIG, loggingConfig, false);
    }
}
