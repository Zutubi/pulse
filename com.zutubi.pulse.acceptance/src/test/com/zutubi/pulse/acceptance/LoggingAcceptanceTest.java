package com.zutubi.pulse.acceptance;

import com.zutubi.pulse.master.bootstrap.SimpleMasterConfigurationManager;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.tove.type.record.PathUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Hashtable;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;

public class LoggingAcceptanceTest extends AcceptanceTestBase
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
        xmlRpcHelper.loginAsAdmin();
        disableExtraLogging();
        logDir = new File(xmlRpcHelper.getServerInfo().get(SimpleMasterConfigurationManager.CORE_PROPERTY_PULSE_LOG_DIR));
    }

    @Override
    protected void tearDown() throws Exception
    {
        disableExtraLogging();
        xmlRpcHelper.logout();
        logDir = null;
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
        long originalLength = fileLength(firstEventLog);

        // A config change will set off some events.
        makeConfigChange();

        assertEquals(originalLength, fileLength(firstEventLog));

        setEventLoggingEnabled(true);
        makeConfigChange();

        assertThat(fileLength(firstEventLog), greaterThan(originalLength));
        String content = tail(firstEventLog, originalLength);
        assertThat(content, containsString("Save Event: " + GlobalConfiguration.SCOPE_NAME));
    }

    public void testEnableConfigLogging() throws Exception
    {
        File firstConfigLog = new File(logDir, "config0.0.log");
        long originalLength = fileLength(firstConfigLog);

        makeConfigChange();

        assertEquals(originalLength, fileLength(firstConfigLog));

        setConfigLoggingEnabled(true);
        makeConfigChange();

        assertThat(fileLength(firstConfigLog), greaterThan(originalLength));
        String content = tail(firstConfigLog, originalLength);
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

    /**
     * Return the contents of the file from the specified location.
     *
     * @param f         the file from which the contents are being read.
     * @param location  the start location (in bytes) for the data to be read.
     * @return  the contents of the file from the specified location
     * @throws IOException  on error.
     */
    private String tail(File f, long location) throws IOException
    {
        RandomAccessFile raf = new RandomAccessFile(f, "r");
        raf.seek(location);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int n;

        while (!Thread.interrupted() && (n = raf.read(buffer)) > 0)
        {
            output.write(buffer, 0, n);
        }

        return new String(output.toByteArray());
    }

    // For some unknown reason, f.length() can return an out of date value
    // on windows 7 (or at least that is the behaviour suggested by test
    // failures on windows 7 - pausing was not helping).  The RandomAccessFile
    // seems to not suffer from this same behaviour.
    private long fileLength(File f) throws IOException
    {
        return new RandomAccessFile(f, "r").length();
    }
}
