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

package com.zutubi.pulse.master.scm;

import com.zutubi.pulse.core.marshal.FileResolver;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmContext;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.tove.type.record.PathUtils;

import java.io.InputStream;

/**
 * Resolves files from a location on an SCM server.
 */
public class ScmFileResolver implements FileResolver
{
    private Project project;
    private Revision revision;
    private ScmManager scmManager;

    public ScmFileResolver(Project project, Revision revision, ScmManager scmManager)
    {
        this.project = project;
        this.revision = revision;
        this.scmManager = scmManager;
    }

    public InputStream resolve(final String path) throws Exception
    {
        return ScmClientUtils.withScmClient(project.getConfig(), project.getState(), scmManager, new ScmClientUtils.ScmContextualAction<InputStream>()
        {
            public InputStream process(ScmClient client, ScmContext context) throws ScmException
            {
                String relativePath = path.startsWith(PathUtils.SEPARATOR) ? path.substring(PathUtils.SEPARATOR.length()) : path;
                return client.retrieve(context, relativePath, revision);
            }
        });
    }
}
