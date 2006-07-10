package com.zutubi.pulse.web.fs;

import java.io.FileFilter;
import java.io.File;

/**
 * <class-comment/>
 */
public class HiddenFileFilter implements FileFilter
{
    public boolean accept(File f)
    {
        return !f.isHidden();
    }
}
