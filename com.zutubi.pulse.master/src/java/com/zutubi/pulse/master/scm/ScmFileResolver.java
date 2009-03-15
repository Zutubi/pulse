package com.zutubi.pulse.master.scm;

import com.zutubi.pulse.core.marshal.FileResolver;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmContext;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.type.record.PathUtils;

import java.io.InputStream;

/**
 * Resolves files from a location on an SCM server.
 */
public class ScmFileResolver implements FileResolver
{
    private ProjectConfiguration projectConfiguration;
    private Revision revision;
    private ScmManager scmManager;

    public ScmFileResolver(ProjectConfiguration projectConfiguration, Revision revision, ScmManager scmManager)
    {
        this.projectConfiguration = projectConfiguration;
        this.revision = revision;
        this.scmManager = scmManager;
    }

    public InputStream resolve(final String path) throws Exception
    {
        return ScmClientUtils.withScmClient(projectConfiguration, scmManager, new ScmClientUtils.ScmContextualAction<InputStream>()
        {
            public InputStream process(ScmClient client, ScmContext context) throws ScmException
            {
                String relativePath = path.startsWith(PathUtils.SEPARATOR) ? path.substring(PathUtils.SEPARATOR.length()) : path;
                return client.retrieve(context, relativePath, revision);
            }
        });
    }
}
