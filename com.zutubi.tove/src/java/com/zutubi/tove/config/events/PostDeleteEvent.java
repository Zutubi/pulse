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
 * Raised when an instance has been deleted and the transaction is being
 * committed.  Note that you should not interact with the configuration
 * system while handling this event.  If you need to interact with the
 * configuration system, consider handling {@link DeleteEvent}.
 */
public class PostDeleteEvent extends CascadableEvent
{
    public PostDeleteEvent(ConfigurationTemplateManager source, Configuration oldInstance, boolean cascaded)
    {
        super(source, oldInstance, cascaded);
    }

    public boolean isPost()
    {
        return true;
    }

    public String toString()
    {
        return "Post Delete Event: " + getInstance().getConfigurationPath();
    }
}
