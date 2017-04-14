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

package com.zutubi.pulse.master.scheduling;

/**
 * A trigger that fires based on a Cron-like expression.
 */
public class CronTrigger extends Trigger
{
    protected static final String TYPE = "cron";

    private String cron;

    public CronTrigger()
    {

    }

    public CronTrigger(String cron, String name)
    {
        this(cron, name, DEFAULT_GROUP);
    }

    public CronTrigger(String cron, String name, String group)
    {
        super(name, group);
        this.cron = cron;
    }

    public String getType()
    {
        return TYPE;
    }

    public String getCron()
    {
        return cron;
    }

    public void setCron(String cron)
    {
        this.cron = cron;
    }
}
