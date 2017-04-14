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
import org.netbeans.lib.cvsclient.event.FileAddedEvent;

import java.io.File;

/**
 * An adapter between the CVS library and our SCM API listener interfaces.
 */
public class CheckoutListener extends BootstrapListener
{
    public CheckoutListener(ScmFeedbackHandler handler, File workingDirectory)
    {
        super(handler, workingDirectory);
    }

    public void fileAdded(FileAddedEvent e)
    {
        reportStatus("U", new File(e.getFilePath()));
    }
}

