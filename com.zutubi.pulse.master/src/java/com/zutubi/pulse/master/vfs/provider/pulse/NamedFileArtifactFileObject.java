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
import com.zutubi.util.io.FileSystemUtils;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.io.File;

/**
 * <class comment/>
 */
public class NamedFileArtifactFileObject extends AbstractPulseFileObject implements AddressableFileObject
{
    private File file;

    public NamedFileArtifactFileObject(final FileName name, final File file, final AbstractFileSystem fs)
    {
        super(name, fs);

        this.file = file;
    }

    public AbstractPulseFileObject createFile(final FileName fileName)
    {
        String name = fileName.getBaseName();
        File newFile = new File(file, name);
        
        return objectFactory.buildBean(NamedFileArtifactFileObject.class, fileName, newFile, pfs);
    }

    protected FileType doGetType() throws Exception
    {
        if (file.isDirectory())
        {
            return FileType.FOLDER;
        }
        if (file.isFile())
        {
            return FileType.FILE;
        }
        return FileType.IMAGINARY;
    }

    protected String[] doListChildren() throws Exception
    {
        return new String[0];
    }

    public boolean isLocal()
    {
        return true;
    }

    public String getUrlPath() throws FileSystemException
    {
        if (file.isDirectory())
        {
            throw new FileSystemException("Displaying of artifact directories is not supported.");
        }

        String basePath = getArtifactBase().getAbsolutePath();
        String artifactPath = file.getAbsolutePath();
        String path = artifactPath.substring(basePath.length() + 1);
        path = FileSystemUtils.normaliseSeparators(path);

        return "/file/artifacts/" + getArtifact().getId() + "/" + path;
    }

    private File getArtifactBase() throws FileSystemException
    {
        CommandResult result = getCommandResult();
        StoredArtifact artifact = getArtifact();

        File outputDir = result.getAbsoluteOutputDir(pfs.getConfigurationManager().getDataDirectory());
        return new File(outputDir, artifact.getName());
    }

    protected StoredArtifact getArtifact() throws FileSystemException
    {
        ArtifactProvider provider = getAncestor(ArtifactProvider.class);
        return provider.getArtifact();
    }

    protected CommandResult getCommandResult() throws FileSystemException
    {
        CommandResultProvider provider = getAncestor(CommandResultProvider.class);
        return provider.getCommandResult();
    }
}
