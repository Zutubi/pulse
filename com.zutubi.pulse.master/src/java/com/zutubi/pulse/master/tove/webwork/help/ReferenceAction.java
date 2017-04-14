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

package com.zutubi.pulse.master.tove.webwork.help;

import com.zutubi.pulse.core.marshal.doc.NodeDocs;
import com.zutubi.pulse.master.vfs.VfsManagerFactoryBean;
import com.zutubi.pulse.master.vfs.provider.pulse.reference.*;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.StringUtils;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;

/**
 * Action to load reference documentation for a node in a Pulse file.
 */
public class ReferenceAction extends ActionSupport
{
    private static final String RESULT_BUILTIN = "builtin";
    private static final String RESULT_ELEMENT = "element";
    private static final String RESULT_EXTENSIBLE = "extensible";
    private static final String RESULT_FILETYPE = "filetype";
    private static final String RESULT_ROOT = "root";
    private static final String RESULT_STATIC = "static";

    private String path;
    private String parentPath;
    private String staticPath;
    private String baseName;
    private NodeDocs docs;

    private FileSystemManager fileSystemManager;

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String getParentPath()
    {
        return parentPath;
    }

    public String getBaseName()
    {
        return baseName;
    }

    public String getStaticPath()
    {
        return staticPath;
    }

    public NodeDocs getDocs()
    {
        return docs;
    }

    @Override
    public String execute() throws Exception
    {
        if (!StringUtils.stringSet(path))
        {
            throw new IllegalArgumentException("Required parameter 'path' not specified.");
        }

        String absolutePath = VfsManagerFactoryBean.FS_PULSE + "://" + path;
        FileObject fileObject = fileSystemManager.resolveFile(absolutePath);
        if (fileObject == null)
        {
            throw new IllegalArgumentException("Invalid path '" + path + "'");
        }

        baseName = PathUtils.getBaseName(path);
        parentPath = PathUtils.getParentPath(path);

        if (fileObject instanceof ReferenceRootFileObject)
        {
            return RESULT_ROOT;
        }
        if (fileObject instanceof FileTypeFileObject)
        {
            return RESULT_FILETYPE;
        }
        else if (fileObject instanceof ElementFileObject)
        {
            docs = ((ElementFileObject) fileObject).getElementDocs();
            return RESULT_ELEMENT;
        }
        else if (fileObject instanceof BuiltinElementFileObject)
        {
            docs = ((BuiltinElementFileObject) fileObject).getElementDocs();
            return RESULT_BUILTIN;
        }
        else if (fileObject instanceof ExtensibleFileObject)
        {
            docs = ((ExtensibleFileObject) fileObject).getExtensibleDocs();
            return RESULT_EXTENSIBLE;
        }
        else
        {
            staticPath = ((StaticReferenceFileObject) fileObject).getStaticPath();
            return RESULT_STATIC;
        }
    }

    public void setFileSystemManager(FileSystemManager fileSystemManager)
    {
        this.fileSystemManager = fileSystemManager;
    }
}
