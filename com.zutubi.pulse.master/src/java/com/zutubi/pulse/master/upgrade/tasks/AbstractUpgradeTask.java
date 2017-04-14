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

package com.zutubi.pulse.master.upgrade.tasks;

import java.util.LinkedList;
import java.util.List;

/**
 * The AbstractUpgradeTask is an implementation of the PulseUpgradeTask
 * interface that provides default implementations for the boiler plate
 * methods.
 */
public abstract class AbstractUpgradeTask implements PulseUpgradeTask
{
    protected final UpgradeTaskMessages I18N;

    private int buildNumber;

    private List<String> errors;

    public AbstractUpgradeTask()
    {
        I18N = new UpgradeTaskMessages(getClass());
    }

    public String getName()
    {
        return I18N.getName();
    }

    public String getDescription()
    {
        return I18N.getDescription();
    }

    public int getBuildNumber()
    {
        return buildNumber;
    }

    public void setBuildNumber(int buildNumber)
    {
        this.buildNumber = buildNumber;
    }

    public boolean hasFailed()
    {
        return getErrors().size() > 0;
    }

    protected void addError(String msg)
    {
        getErrors().add(msg);
    }

    public List<String> getErrors()
    {
        if (errors == null)
        {
            synchronized(this)
            {
                if (errors == null)
                {
                    errors = new LinkedList<String>();
                }
            }
        }
        return errors;
    }
}
