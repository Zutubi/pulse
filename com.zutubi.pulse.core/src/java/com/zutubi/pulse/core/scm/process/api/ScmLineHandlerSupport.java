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

package com.zutubi.pulse.core.scm.process.api;

import com.zutubi.pulse.core.scm.api.ScmFeedbackHandler;

/**
 * A helper base class for implementing {@link ScmLineHandler}.  Adapts
 * feedback to an {@link ScmFeedbackHandler} when one is available.
 */
public class ScmLineHandlerSupport extends ScmOutputHandlerSupport implements ScmLineHandler
{
    /**
     * Creates a handler with no underlying {@link ScmFeedbackHandler}.
     */
    public ScmLineHandlerSupport()
    {
    }

    /**
     * Creates a handler with the given underlying {@link ScmFeedbackHandler}.
     * 
     * @param feedbackHandler handler that will be passed status messages
     */
    public ScmLineHandlerSupport(ScmFeedbackHandler feedbackHandler)
    {
        super(feedbackHandler);
    }

    public void handleStdout(String line)
    {
        status(line);
    }

    public void handleStderr(String line)
    {
        status(line);
    }
}
