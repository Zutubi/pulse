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

package com.zutubi.pulse.dev.personal;

import com.zutubi.pulse.core.scm.api.WorkingCopy;
import com.zutubi.pulse.core.scm.api.WorkingCopyContext;
import com.zutubi.pulse.core.scm.patch.api.PatchFormat;
import com.zutubi.pulse.dev.client.ClientException;

/**
 * Simple holder class for objects used during a personal build.
 */
public class PersonalBuildContext
{
    private WorkingCopy workingCopy;
    private WorkingCopyContext workingCopyContext;
    private String patchFormatType;
    private PatchFormat patchFormat;

    public PersonalBuildContext(WorkingCopy workingCopy, WorkingCopyContext workingCopyContext, String patchFormatType, PatchFormat patchFormat)
    {
        this.workingCopy = workingCopy;
        this.workingCopyContext = workingCopyContext;
        this.patchFormatType = patchFormatType;
        this.patchFormat = patchFormat;
    }

    public WorkingCopy getWorkingCopy()
    {
        return workingCopy;
    }

    public WorkingCopy getRequiredWorkingCopy() throws ClientException
    {
        if (workingCopy == null)
        {
            throw new ClientException("Personal builds are not supported for this SCM.");
        }

        return workingCopy;
    }

    public WorkingCopyContext getWorkingCopyContext()
    {
        return workingCopyContext;
    }

    public String getPatchFormatType()
    {
        return patchFormatType;
    }

    public PatchFormat getPatchFormat()
    {
        return patchFormat;
    }
}
