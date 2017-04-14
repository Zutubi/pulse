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

import com.zutubi.pulse.core.api.PulseException;
import com.zutubi.pulse.master.notifications.jabber.JabberManager;
import com.zutubi.pulse.master.notifications.renderer.RenderedResult;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wire;
import com.zutubi.tove.config.api.AbstractConfigurationCheckHandler;

@Wire
@SymbolicName("zutubi.jabberContactConfigurationCheckHandler")
public class JabberContactConfigurationCheckHandler extends AbstractConfigurationCheckHandler<JabberContactConfiguration>
{
    private JabberManager jabberManager;

    public void test(JabberContactConfiguration configuration) throws Exception
    {
        if (!jabberManager.isConfigured())
        {
            throw new PulseException("Jabber is disabled.");
        }
        configuration.notify(new RenderedResult(null, "Test message from Pulse", null), null);
    }

    public void setJabberManager(JabberManager jabberManager)
    {
        this.jabberManager = jabberManager;
    }
}
