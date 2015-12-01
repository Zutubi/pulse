package com.zutubi.pulse.master.rest.model.setup;

import com.zutubi.pulse.master.rest.model.TransientModel;
import com.zutubi.pulse.master.util.monitor.Monitor;

import java.util.HashMap;
import java.util.Map;

/**
 * Model to hold setup state of the server.
 */
public class SetupModel
{
    private String status;
    private TransientModel input;
    private ProgressModel progress;
    private Map<String, Object> properties;

    public SetupModel(String status)
    {
        this.status = status;
    }

    public String getStatus()
    {
        return status;
    }

    public TransientModel getInput()
    {
        return input;
    }

    public void setInput(TransientModel input)
    {
        this.input = input;
    }

    public ProgressModel getProgress()
    {
        return progress;
    }

    public void setProgressMonitor(Monitor monitor)
    {
        if (monitor != null)
        {
            progress = new ProgressModel(monitor);
        }
    }

    public Map<String, Object> getProperties()
    {
        return properties;
    }

    public void addProperty(String name, Object value)
    {
        if (properties == null)
        {
            properties = new HashMap<>();
        }

        properties.put(name, value);
    }
}
