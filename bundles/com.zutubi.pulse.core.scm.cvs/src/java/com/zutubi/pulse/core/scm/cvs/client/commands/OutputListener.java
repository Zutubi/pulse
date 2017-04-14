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

import org.netbeans.lib.cvsclient.event.CVSAdapter;
import org.netbeans.lib.cvsclient.event.MessageEvent;

import java.io.PrintWriter;

/**
 * A CVS listener that just writes all output to a PrintWriter.
 */
public class OutputListener extends CVSAdapter
{
    private PrintWriter writer;
    private final StringBuffer taggedLine = new StringBuffer();

    public OutputListener(PrintWriter writer)
    {
        this.writer = writer;
    }

    public void messageSent(MessageEvent e)
    {
        if (!e.isError())
        {
            String line = e.getMessage();
            if (e.isTagged())
            {
                String message = MessageEvent.parseTaggedMessage(taggedLine, line);
                if (message != null)
                {
                    writer.println(message);
                    taggedLine.setLength(0);
                }
            }
            else
            {
                writer.println(line);
            }
        }
    }

}
