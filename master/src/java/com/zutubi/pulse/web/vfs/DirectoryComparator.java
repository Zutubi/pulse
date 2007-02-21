package com.zutubi.pulse.web.vfs;

import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.logging.Logger;
import com.zutubi.pulse.vfs.pulse.AbstractPulseFileObject;

import java.io.File;
import java.util.Comparator;
import java.text.Collator;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.FileSystemException;

/**
 * <class-comment/>
 */
public class DirectoryComparator implements Comparator<FileObject>
{
    private static final Logger LOG = Logger.getLogger(DirectoryComparator.class);

    private Collator c = Collator.getInstance();

    public int compare(FileObject o1, FileObject o2)
    {
        // folders first.
        try
        {
            FileType t1 = o1.getType();
            FileType t2 = o2.getType();

            if ((t1 == FileType.FOLDER || t1 == FileType.IMAGINARY) &&
                    (t2 != FileType.FOLDER && t2 != FileType.IMAGINARY))
            {
                return -1;
            }
            if ((t2 == FileType.FOLDER || t2 == FileType.IMAGINARY) &&
                    (t1 != FileType.FOLDER && t1 != FileType.IMAGINARY))
            {
                return 1;
            }

            // then sort alphabetically
            if(o1 instanceof AbstractPulseFileObject)
            {
                return c.compare(((AbstractPulseFileObject)o1).getDisplayName(), ((AbstractPulseFileObject)o2).getDisplayName());
            }

            return c.compare(o1.getName().getPath(), o2.getName().getPath());
        }
        catch (FileSystemException e)
        {
            LOG.error(e);
            return 0;
        }
    }

}
