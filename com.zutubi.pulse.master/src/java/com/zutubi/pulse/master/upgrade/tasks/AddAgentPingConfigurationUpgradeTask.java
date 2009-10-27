package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

/**
 * Adds configuration for agent pings to the global settings.
 */
public class AddAgentPingConfigurationUpgradeTask extends AbstractUpgradeTask
{
    private RecordManager recordManager;

    public void execute() throws TaskException
    {
        Record globalSettings = recordManager.select("settings");
        if (globalSettings.containsKey("agentPing"))
        {
            // ALready ran (probably in 2.0, and we are 2.1).
            return;
        }

        MutableRecord newRecord = new MutableRecordImpl();
        newRecord.setSymbolicName("zutubi.agentPingConfig");
        newRecord.put("maxConcurrent", "12");
        newRecord.put("pingInterval", System.getProperty("pulse.agent.ping.interval", "60"));
        newRecord.put("pingTimeout", System.getProperty("pulse.agent.ping.timeout", "45"));
        newRecord.put("offlineTimeout", getOfflineTimeout());
        newRecord.put("timeoutLoggingEnabled", System.getProperty("pulse.agent.log.timeouts", Boolean.TRUE.toString()));

        recordManager.insert("settings/agentPing", newRecord);
    }

    private String getOfflineTimeout()
    {
        String offline = System.getProperty("pulse.agent.offline.timeout");
        if (offline == null)
        {
            int pingInterval = Integer.parseInt(System.getProperty("pulse.agent.ping.interval", "60"));
            offline = Integer.toString(pingInterval * 4);
        }

        return offline;
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}