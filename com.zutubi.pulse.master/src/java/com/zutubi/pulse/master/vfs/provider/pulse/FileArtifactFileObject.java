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

import com.zutubi.pulse.core.model.CommandResult;
import com.zutubi.pulse.core.model.StoredArtifact;
import com.zutubi.pulse.core.model.StoredFileArtifact;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.webwork.Urls;
import com.zutubi.util.StringUtils;
import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.logging.Logger;
import org.apache.commons.vfs.FileContentInfoFactory;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.UriParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Represents a single file or directory of an artifact in an artifacts tree.
 */
public class FileArtifactFileObject extends AbstractPulseFileObject implements AddressableFileObject, FileArtifactProvider
{
    private static final Logger LOG = Logger.getLogger(FileArtifactFileObject.class);

    private static final String NO_HASH = "";

    private File base;
    private Boolean canDecorate;

    public FileArtifactFileObject(final FileName name, final File base, final AbstractFileSystem fs)
    {
        super(name, fs);

        this.base = base;
    }

    private boolean canDecorate()
    {
        if (canDecorate == null)
        {
            canDecorate = false;
            try
            {
                if(base.isFile())
                {
                    StoredFileArtifact artifact = getFileArtifact();
                    if (artifact != null)
                    {
                        canDecorate = artifact.canDecorate();
                    }
                }
            }
            catch (FileSystemException e)
            {
                LOG.error(e);
            }
        }
        return canDecorate;
    }

    public AbstractPulseFileObject createFile(final FileName fileName)
    {
        File newBase = new File(base, fileName.getBaseName());

        return objectFactory.buildBean(FileArtifactFileObject.class, fileName, newBase, pfs);
    }

    protected FileType doGetType() throws Exception
    {
        if (base.isDirectory())
        {
            return FileType.FOLDER;
        }
        return FileType.FILE;
    }

    protected String[] doListChildren() throws Exception
    {
        return UriParser.encode(FileSystemUtils.list(base));
    }

    protected long doGetContentSize()
    {
        return base.length();
    }

    @Override
    protected FileContentInfoFactory getFileContentInfoFactory()
    {
        try
        {
            final StoredFileArtifact fileArtifact = getFileArtifact();
            if (fileArtifact != null && StringUtils.stringSet(fileArtifact.getType()))
            {
                return new FixedTypeFileContentInfoFactory(fileArtifact.getType());
            }
        }
        catch (FileSystemException e)
        {
            LOG.warning(e);
        }

        return super.getFileContentInfoFactory();
    }

    protected InputStream doGetInputStream() throws Exception
    {
        return new FileInputStream(base);
    }

    public File toFile()
    {
        return base;
    }

    public boolean isLocal()
    {
        return true;
    }

    public String getUrlPath()
    {
        if (base.isDirectory())
        {
            return "";
        }

        try
        {
            return "/file/artifacts/" + getArtifact().getId() + "/" + getArtifactPath();
        }
        catch (FileSystemException e)
        {
            LOG.warning(e);
            return "";
        }
    }

    public List<FileAction> getActions()
    {
        List<FileAction> actions = new LinkedList<FileAction>();
        if (base.isFile())
        {
            actions.add(new FileAction(FileAction.TYPE_DOWNLOAD, getUrlPath()));
        }
        if (canDecorate())
        {
            try
            {
                BuildResult build = getAncestor(BuildResultProvider.class).getBuildResult();
                CommandResult command = getAncestor(CommandResultProvider.class).getCommandResult();
                String url = Urls.getBaselessInstance().commandArtifacts(build, command) + getFileArtifact().getPathUrl() + "/";
                actions.add(new FileAction(FileAction.TYPE_DECORATE, url));
            }
            catch (Exception e)
            {
                LOG.warning(e);
            }
        }
        return actions;
    }

    private String getHash()
    {
        try
        {
            StoredFileArtifact fileArtifact = getFileArtifact();
            if (fileArtifact != null)
            {
                String hash = fileArtifact.getHash();
                if (hash != null)
                {
                    return hash;
                }
            }
        }
        catch (FileSystemException e)
        {
            // Fall through.
        }
        return NO_HASH;
    }

    public StoredFileArtifact getFileArtifact() throws FileSystemException
    {
        return getArtifact().findFileBase(getArtifactPath());
    }

    public File getFile()
    {
        return base;
    }

    public long getFileArtifactId() throws FileSystemException
    {
        return getFileArtifact().getId();
    }

    protected StoredArtifact getArtifact() throws FileSystemException
    {
        return getAncestor(ArtifactProvider.class).getArtifact();
    }

    protected String getArtifactPath() throws FileSystemException
    {
        AbstractPulseFileObject fo = (AbstractPulseFileObject) getAncestor(ArtifactProvider.class);
        return fo.getName().getRelativeName(getName());
    }

    @Override
    public Map<String, Object> getExtraAttributes()
    {
        Map<String, Object> result = new HashMap<String, Object>();
        if (base.isFile())
        {
            result.put("hash", getHash());
            result.put("size", Long.toString(doGetContentSize()));
        }

        result.put("actions", getActions());
        return result;
    }
}
