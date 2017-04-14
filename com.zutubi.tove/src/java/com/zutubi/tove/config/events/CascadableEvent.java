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

package com.zutubi.tove.config.events;

import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.config.ConfigurationTemplateManager;

/**
 */
public abstract class CascadableEvent extends ConfigurationEvent
{
    private boolean cascaded;

    protected CascadableEvent(ConfigurationTemplateManager source, Configuration instance, boolean cascaded)
    {
        super(source, instance);
        this.cascaded = cascaded;
    }

    public boolean isCascaded()
    {
        return cascaded;
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        CascadableEvent event = (CascadableEvent) o;
        return cascaded == event.cascaded;
    }

    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (cascaded ? 1 : 0);
        return result;
    }
}
