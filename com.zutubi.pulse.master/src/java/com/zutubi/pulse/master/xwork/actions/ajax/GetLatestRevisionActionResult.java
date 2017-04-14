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

package com.zutubi.pulse.master.xwork.actions.ajax;

/**
 * Object representing the JSON result for the GetLatestRevisionAction.
 *
 * @see com.zutubi.pulse.master.webwork.dispatcher.FlexJsonResult
 */
public class GetLatestRevisionActionResult
{
    private boolean successful = false;
    private String latestRevision;
    private String error;

    public void setSuccessful(boolean successful)
    {
        this.successful = successful;
    }

    public void setLatestRevision(String latestRevision)
    {
        this.latestRevision = latestRevision;
    }

    public void setError(String error)
    {
        this.error = error;
    }

    public boolean isSuccessful()
    {
        return successful;
    }

    public String getLatestRevision()
    {
        return latestRevision;
    }

    public String getError()
    {
        return error;
    }
}
