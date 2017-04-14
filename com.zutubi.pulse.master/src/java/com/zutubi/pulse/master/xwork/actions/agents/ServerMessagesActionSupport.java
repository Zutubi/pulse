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

package com.zutubi.pulse.master.xwork.actions.agents;

import com.zutubi.pulse.servercore.util.logging.CustomLogRecord;
import com.zutubi.pulse.servercore.util.logging.ServerMessagesHandler;

import java.util.logging.Level;

/**
 * Helper base class for actions that display server messages.
 */
public class ServerMessagesActionSupport extends AgentActionBase
{
    protected ServerMessagesHandler serverMessagesHandler;

    public boolean isError(CustomLogRecord record)
    {
        return record.getLevel().intValue() == Level.SEVERE.intValue();
    }

    public boolean isWarning(CustomLogRecord record)
    {
        return record.getLevel().intValue() == Level.WARNING.intValue();
    }

    public boolean hasThrowable(CustomLogRecord record)
    {
        return record.getStackTrace().length() > 0;
    }

    public void setServerMessagesHandler(ServerMessagesHandler serverMessagesHandler)
    {
        this.serverMessagesHandler = serverMessagesHandler;
    }
}
