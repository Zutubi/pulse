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

package com.zutubi.pulse.master.xwork.actions.vfs;

import com.zutubi.pulse.core.api.PulseRuntimeException;
import com.zutubi.pulse.master.vfs.provider.pulse.AbstractPulseFileObject;
import com.zutubi.pulse.master.vfs.provider.pulse.AddressableFileObject;
import com.zutubi.pulse.master.vfs.provider.pulse.FileAction;
import com.zutubi.util.logging.Logger;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.UriParser;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <class comment/>
 */
public class FileObjectWrapper
{
    private static final Logger LOG = Logger.getLogger(FileObjectWrapper.class);

    private FileObject fo;
    private FileObject base;

    public FileObjectWrapper(FileObject fo, FileObject base)
    {
        this.fo = fo;
        this.base = base;
    }

    public String getName()
    {
        try
        {
            if (fo instanceof AbstractPulseFileObject)
            {
                return ((AbstractPulseFileObject)fo).getDisplayName();
            }
            return UriParser.decode(fo.getName().getBaseName());
        }
        catch (FileSystemException e)
        {
            throw new PulseRuntimeException(e);
        }
    }

    public String getBaseName()
    {
        try
        {
            return UriParser.decode(fo.getName().getBaseName());
        }
        catch (FileSystemException e)
        {
            throw new PulseRuntimeException(e);
        }
    }

    public String getUrl()
    {
        try
        {
            if (fo instanceof AddressableFileObject)
            {
                return ((AddressableFileObject)fo).getUrlPath();
            }
        }
        catch (FileSystemException e)
        {
            //noop.
        }
        return "";
    }

    public String getId()
    {
        String baseName = fo.getName().getBaseName();
        try
        {
            return UriParser.decode(baseName);
        }
        catch (FileSystemException e)
        {
            throw new PulseRuntimeException(e);
        }
    }

    public String getRelativeParentPath()
    {
        try
        {
            FileObject parent = fo.getParent();
            if(parent != null)
            {
                String path = base.getName().getRelativeName(parent.getName());
                if(path.equals("."))
                {
                    path = "";
                }
                return path;
            }
        }
        catch (FileSystemException e)
        {
            LOG.severe(e);
        }
        return "";
    }

    public boolean isContainer()
    {
        try
        {
            return fo.getType() == FileType.FOLDER;
        }
        catch (FileSystemException e)
        {
            return false;
        }
    }

    public String getSeparator()
    {
        return File.separator;
    }

    public List<FileAction> getActions()
    {
        if (fo instanceof AbstractPulseFileObject)
        {
            return ((AbstractPulseFileObject)fo).getActions();
        }
        return Collections.emptyList();
    }

    public String getCls()
    {
        if (fo instanceof AbstractPulseFileObject)
        {
            return ((AbstractPulseFileObject)fo).getCls();
        }
        return null;
    }

    public String getIconCls()
    {
        try
        {
            if (fo instanceof AbstractPulseFileObject)
            {
                return ((AbstractPulseFileObject)fo).getIconCls();
            }
        }
        catch (Exception e)
        {
            // noop.
        }
        return null;
    }

    public Map<String, Object> getExtraAttributes()
    {
        if (fo instanceof AbstractPulseFileObject)
        {
            return ((AbstractPulseFileObject)fo).getExtraAttributes();
        }
        return null;
    }
}
