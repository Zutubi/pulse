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

import com.zutubi.pulse.core.scm.api.ScmFeedbackHandler;
import org.netbeans.lib.cvsclient.command.DefaultFileInfoContainer;
import org.netbeans.lib.cvsclient.command.FileInfoContainer;
import org.netbeans.lib.cvsclient.event.FileInfoEvent;
import org.netbeans.lib.cvsclient.event.FileRemovedEvent;

import java.io.File;

/**
 * <class comment/>
 */
public class UpdateListener extends BootstrapListener
{

    public UpdateListener(ScmFeedbackHandler handler, File workingDirectory)
    {
        super(handler, workingDirectory);
    }

    public void fileRemoved(FileRemovedEvent evt)
    {
        reportStatus("D", new File(evt.getFilePath()));
    }

    public void fileInfoGenerated(FileInfoEvent evt)
    {
        FileInfoContainer c = evt.getInfoContainer();
        if (!(c instanceof DefaultFileInfoContainer))
        {
            return;
        }

        DefaultFileInfoContainer infoContainer = (DefaultFileInfoContainer) evt.getInfoContainer();
        if ("U".equals(infoContainer.getType()))
        {
            reportStatus("U", infoContainer.getFile());
        }
    }
}
