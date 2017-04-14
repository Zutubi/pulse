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
 * Raised when an instance is being deleted.  Note that the delete may later
 * fail and the transaction be rolled back.  Thus this event is most useful
 * when the handler makes other changes that should only be committed if the
 * delete goes ahead.  To only react when the transaction is certain to
 * commit handle {@link PostDeleteEvent}.
 */
public class DeleteEvent extends CascadableEvent
{
    public DeleteEvent(ConfigurationTemplateManager source, Configuration oldInstance, boolean cascaded)
    {
        super(source, oldInstance, cascaded);
    }

    public boolean isPost()
    {
        return false;
    }

    public String toString()
    {
        return "Delete Event: " + getInstance().getConfigurationPath();
    }
}
