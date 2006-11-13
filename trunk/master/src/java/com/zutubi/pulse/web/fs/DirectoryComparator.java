package com.zutubi.pulse.web.fs;

import com.zutubi.pulse.util.FileSystemUtils;

import java.util.Comparator;
import java.io.File;
import java.text.Collator;

/**
 * <class-comment/>
 */
public class DirectoryComparator implements Comparator<File>
{
    private Collator c = Collator.getInstance();

    public int compare(File o1, File o2)
    {
        // folders first.
        if ((o1.isDirectory() || FileSystemUtils.isRoot(o1)) &&
                (!o2.isDirectory() && !FileSystemUtils.isRoot(o2)))
        {
            return -1;
        }
        if ((o2.isDirectory() || FileSystemUtils.isRoot(o2)) &&
                (!o1.isDirectory() && !FileSystemUtils.isRoot(o1)))
        {
            return 1;
        }

        // then sort alphabetically
        return c.compare(o1.getAbsolutePath(), o2.getAbsolutePath());
    }

}
