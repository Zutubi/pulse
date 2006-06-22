package com.zutubi.pulse.web.fs;

import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.filesystem.FileSystem;

/**
 * <class-comment/>
 */
public abstract class FileSystemActionSupport extends ActionSupport
{
    public abstract FileSystem getFileSystem();
}
