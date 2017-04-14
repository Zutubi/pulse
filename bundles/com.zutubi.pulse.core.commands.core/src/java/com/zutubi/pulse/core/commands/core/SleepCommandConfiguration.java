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

package com.zutubi.pulse.core.commands.core;

import com.zutubi.pulse.core.commands.api.CommandConfigurationSupport;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.validation.annotations.Min;

/**
 * Configuration for instances of {@link SleepCommand}.
 */
@SymbolicName("zutubi.sleepCommandConfig")
@Form(fieldOrder = {"name", "interval", "force"})
public class SleepCommandConfiguration extends CommandConfigurationSupport
{
    @Min(0)
    private int interval;

    public SleepCommandConfiguration()
    {
        super(SleepCommand.class);
    }

    public SleepCommandConfiguration(String name)
    {
        this();
        setName(name);
    }

    public int getInterval()
    {
        return interval;
    }

    public void setInterval(int interval)
    {
        this.interval = interval;
    }
}
