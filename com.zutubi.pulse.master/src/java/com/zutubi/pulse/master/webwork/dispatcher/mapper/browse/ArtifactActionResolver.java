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

package com.zutubi.pulse.master.webwork.dispatcher.mapper.browse;

import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolver;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.ActionResolverSupport;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.PathConsumingActionResolver;

import java.util.Arrays;
import java.util.List;

/**
 * Maps to either the raw download or decorated view of an artifact file.
 */
public class ArtifactActionResolver extends ActionResolverSupport
{
    private boolean raw;

    public ArtifactActionResolver(String artifact, boolean raw)
    {
        super("viewBuildArtifacts");
        addParameter("artifactName", artifact);
        this.raw = raw;
    }

    public List<String> listChildren()
    {
        return Arrays.asList("<artifact path>");
    }

    public ActionResolver getChild(String name)
    {
        return new PathConsumingActionResolver(raw ? "downloadArtifact" : "viewArtifact", "path", name);
    }
}
