/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.master.rest.model.setup;

import com.zutubi.pulse.master.util.monitor.Monitor;
import com.zutubi.tove.ui.model.TransientModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Model to hold setup state of the server.
 */
public class SetupModel
{
    private String status;
    private String statusMessage;
    private List<String> errorMessages;
    private TransientModel input;
    private ProgressModel progress;
    private Map<String, Object> properties;

    public SetupModel(String status, String statusMessage)
    {
        this.status = status;
        this.statusMessage = statusMessage;
    }

    public String getStatus()
    {
        return status;
    }

    public String getStatusMessage()
    {
        return statusMessage;
    }

    public List<String> getErrorMessages()
    {
        return errorMessages;
    }

    public void addErrorMessage(String message)
    {
        if (errorMessages == null)
        {
            errorMessages = new ArrayList<>();
        }

        errorMessages.add(message);
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
