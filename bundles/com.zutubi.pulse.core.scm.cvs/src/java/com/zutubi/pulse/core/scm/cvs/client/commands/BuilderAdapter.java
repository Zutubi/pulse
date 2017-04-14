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

package com.zutubi.pulse.core.scm.cvs.client.commands;

import org.netbeans.lib.cvsclient.command.Builder;
import org.netbeans.lib.cvsclient.event.*;

public class BuilderAdapter extends CVSAdapter
{

    public BuilderAdapter(Builder builder)
    {
        taggedLineBuffer = new StringBuffer();
        this.builder = builder;
    }

    public void messageSent(MessageEvent e)
    {
        if(builder == null)
            return;
        if(e instanceof EnhancedMessageEvent)
        {
            EnhancedMessageEvent eEvent = (EnhancedMessageEvent)e;
            builder.parseEnhancedMessage(eEvent.getKey(), eEvent.getValue());
            return;
        }
        if(e.isTagged())
        {
            String message = MessageEvent.parseTaggedMessage(taggedLineBuffer, e.getMessage());
            if(message != null)
            {
                builder.parseLine(message, false);
                taggedLineBuffer.setLength(0);
            }
        } else
        {
            if(taggedLineBuffer.length() > 0)
            {
                builder.parseLine(taggedLineBuffer.toString(), false);
                taggedLineBuffer.setLength(0);
            }
            builder.parseLine(e.getMessage(), e.isError());
        }
    }

    private StringBuffer taggedLineBuffer;
    private final Builder builder;
}
