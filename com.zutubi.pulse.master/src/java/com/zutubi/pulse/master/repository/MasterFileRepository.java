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

package com.zutubi.pulse.master.repository;

import com.zutubi.pulse.master.MasterBuildPaths;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.servercore.repository.FileRepository;

import java.io.File;

/**
 */
public class MasterFileRepository implements FileRepository
{
    private MasterBuildPaths buildPaths;

    public MasterFileRepository(MasterConfigurationManager configurationManager)
    {
        buildPaths = new MasterBuildPaths(configurationManager);
    }

    public File getPatchFile(long userId, long number)
    {
        return buildPaths.getUserPatchFile(userId, number);
    }

}
