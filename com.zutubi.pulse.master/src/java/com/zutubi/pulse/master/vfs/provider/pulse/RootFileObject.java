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

import com.zutubi.pulse.master.vfs.provider.pulse.reference.ReferenceRootFileObject;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.provider.AbstractFileSystem;

/**
 * The root file object of the pulse file system. This file object defines the root
 * 'directories/folders' available within the file system.
 */
public class RootFileObject extends StaticMappingFileObject
{
    public static final String PREFIX_CONFIG        = "c";
    
    public static final String NODE_ARTIFACTS       = "artifacts";
    public static final String NODE_BUILDS          = "builds";
    public static final String NODE_PLUGINS         = "plugins";
    public static final String NODE_PROJECTS        = "projects";
    public static final String NODE_PROJECT_CONFIGS = PREFIX_CONFIG + "projects";
    public static final String NODE_REFERENCE       = "reference";

    static
    {
        // setup the default root node definitions.
        nodesDefinitions.put(NODE_ARTIFACTS, GlobalArtifactsFileObject.class);
        nodesDefinitions.put(NODE_BUILDS, BuildsFileObject.class);
        nodesDefinitions.put(NODE_PLUGINS, PluginsFileObject.class);
        nodesDefinitions.put(NODE_PROJECTS, ProjectsFileObject.class);
        nodesDefinitions.put(NODE_PROJECT_CONFIGS, ProjectConfigsFileObject.class);
        nodesDefinitions.put(NODE_REFERENCE, ReferenceRootFileObject.class);
    }

    public RootFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }
}
