package com.zutubi.pulse.core.scm.git;

import com.zutubi.pulse.core.model.Changelist;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.core.scm.FileStatus;
import com.zutubi.pulse.core.scm.ScmCapability;
import com.zutubi.pulse.core.scm.ScmClient;
import com.zutubi.pulse.core.scm.ScmContext;
import com.zutubi.pulse.core.scm.ScmEventHandler;
import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.core.scm.ScmFile;
import com.zutubi.pulse.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 *
 */
public class GitClient implements ScmClient
{
    private static final Set<ScmCapability> CAPABILITIES = new HashSet<ScmCapability>();
    
    private String repository;

    public void close()
    {

    }

    public Set<ScmCapability> getCapabilities()
    {
        return CAPABILITIES;
    }

    public String getUid() throws ScmException
    {
        return repository;
    }

    public String getLocation() throws ScmException
    {
        return getUid();
    }

    public Revision checkout(ScmContext context, ScmEventHandler handler) throws ScmException
    {
        NativeGit git = new NativeGit();
        File workingDir = context.getDir();
        // git does not like a checkouts into existing directories - not this way anyways.
        if (workingDir.exists() && !FileSystemUtils.rmdir(workingDir))
        {
            throw new ScmException("Failed in clean checkout.  Could not delete directory: " + workingDir.getAbsolutePath());
        }

        git.setWorkingDirectory(workingDir.getParentFile());
        git.clone(repository, workingDir.getName());

        // what is the current head revision?.
        git.setWorkingDirectory(workingDir);
        List<GitLogEntry> entries = git.log("HEAD^", "HEAD");
        GitLogEntry entry = entries.get(0);

        return new Revision(entry.getAuthor(), entry.getComment(), entry.getDate(), entry.getCommit());
    }

    public Revision update(ScmContext context, ScmEventHandler handler) throws ScmException
    {
        return checkout(context, handler);
    }

    public InputStream retrieve(String path, Revision revision) throws ScmException
    {
        return null;
    }

    public void storeConnectionDetails(File outputDir) throws ScmException, IOException
    {

    }

    public FileStatus.EOLStyle getEOLPolicy() throws ScmException
    {
        return null;
    }

    public Revision getLatestRevision() throws ScmException
    {
        return null;
    }

    public List<Revision> getRevisions(Revision from, Revision to) throws ScmException
    {
        return null;
    }

    public List<Changelist> getChanges(Revision from, Revision to) throws ScmException
    {
        return null;
    }

    public List<ScmFile> browse(String path, Revision revision) throws ScmException
    {
        return null;
    }

    public void tag(Revision revision, String name, boolean moveExisting) throws ScmException
    {

    }

    public Revision getRevision(String revision) throws ScmException
    {
        return null;
    }

    public void setRepository(String repository)
    {
        this.repository = repository;
    }
}
