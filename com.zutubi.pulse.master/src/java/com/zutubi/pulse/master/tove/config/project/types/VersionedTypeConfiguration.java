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

package com.zutubi.pulse.master.tove.config.project.types;

import com.zutubi.pulse.core.engine.ExternalPulseFileProvider;
import com.zutubi.pulse.core.engine.PulseFileProvider;
import com.zutubi.pulse.core.tove.config.annotations.BrowseScmFileAction;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Wire;
import com.zutubi.util.io.FileSystemUtils;

/**
 * Pulse file project where the pulse file is stored in the project's SCM.
 */
@SymbolicName("zutubi.versionedTypeConfig")
@Wire
public class VersionedTypeConfiguration extends TypeConfiguration
{
    @BrowseScmFileAction
    private String pulseFileName;

    public String getPulseFileName()
    {
        return pulseFileName;
    }

    public void setPulseFileName(String pulseFileName)
    {
        this.pulseFileName = pulseFileName;
    }

    public PulseFileProvider getPulseFile() throws Exception
    {
        String normalisedPath = FileSystemUtils.normaliseSeparators(pulseFileName);
        return new ExternalPulseFileProvider(normalisedPath);
    }
}
