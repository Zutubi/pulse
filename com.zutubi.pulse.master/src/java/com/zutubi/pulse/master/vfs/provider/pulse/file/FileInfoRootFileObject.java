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

package com.zutubi.pulse.master.vfs.provider.pulse.file;

import com.google.common.base.Function;
import com.zutubi.pulse.master.vfs.provider.pulse.AbstractPulseFileObject;
import com.zutubi.pulse.servercore.filesystem.FileInfo;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.UriParser;

import java.util.List;

import static com.google.common.collect.Collections2.transform;

/**
 * A base implementation for any file object that intends to be the root of a
 * file info subtree.
 */
public abstract class FileInfoRootFileObject extends AbstractPulseFileObject implements FileInfoProvider
{
    private static final String ROOT_PATH = "";

    public FileInfoRootFileObject(FileName name, AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(FileName fileName) throws Exception
    {
        FileInfo child = getFileInfo(fileName.getBaseName());

        return objectFactory.buildBean(FileInfoFileObject.class, child, fileName, pfs);
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        List<FileInfo> children = getFileInfos(ROOT_PATH);
        if (children == null)
        {
            return NO_CHILDREN;
        }
        
        return UriParser.encode(transform(children, new Function<FileInfo, String>()
        {
            public String apply(FileInfo fileInfo)
            {
                return fileInfo.getName();
            }
        }).toArray(new String[children.size()]));
    }
}
