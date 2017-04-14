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

package com.zutubi.pulse.master.tove.config.user.contacts;

import com.zutubi.pulse.master.notifications.NotificationAttachment;
import com.zutubi.pulse.master.notifications.renderer.RenderedResult;
import com.zutubi.tove.annotations.*;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.validation.annotations.Constraint;

import java.util.List;

/**
 * Base for all contact points, establishes the minimal contract they must
 * implement.
 */
@SymbolicName("zutubi.userContactConfig")
@Table(columns = {"name", "uniqueId"})
@Classification(collection = "contacts")
public abstract class ContactConfiguration extends AbstractNamedConfiguration
{
    @Internal @Constraint("OnePrimaryContactValidator")
    private boolean primary;
    
    @Transient
    public abstract String getUniqueId();

    @Transient
    public abstract boolean supportsAttachments();

    public boolean isPrimary()
    {
        return primary;
    }

    public void setPrimary(boolean primary)
    {
        this.primary = primary;
        
        // Marking permanent when primary makes the contact undeletable and
        // uncloneable (both desirable, the latter as the clone would fail to
        // validate due to it being the second primary contact).
        setPermanent(primary);
    }

    public abstract void notify(RenderedResult rendered, List<NotificationAttachment> attachments) throws Exception;
}
