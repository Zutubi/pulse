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

package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.master.model.BuildResult;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * Represents a build where the ID is specified directly.
 */
public class BuildFileObject extends AbstractBuildFileObject
{
    private final long buildId;

    public BuildFileObject(final FileName name, final long buildId, final AbstractFileSystem fs)
    {
        super(name, fs);
        this.buildId = buildId;
    }

    public long getBuildResultId()
    {
        return buildId;
    }

    public BuildResult getBuildResult()
    {
        return buildManager.getBuildResult(buildId);
    }
}
