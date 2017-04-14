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

package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.scm.api.ScmCancelledException;
import com.zutubi.pulse.core.scm.api.ScmFeedbackHandler;

import java.util.LinkedList;
import java.util.List;


/**
 * A test implementation of {@link com.zutubi.pulse.core.scm.api.ScmFeedbackHandler}
 * which just records the feedback.
 */
public class RecordingScmFeedbackHandler implements ScmFeedbackHandler
{
    private List<String> statusMessages = new LinkedList<String>();

    public void reset()
    {
        statusMessages.clear();
    }

    public void status(String message)
    {
        statusMessages.add(message);
    }

    public List<String> getStatusMessages()
    {
        return statusMessages;
    }

    public void checkCancelled() throws ScmCancelledException
    {

    }
}
