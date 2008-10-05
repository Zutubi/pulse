package com.zutubi.pulse.master.xwork.actions.vfs;

import com.zutubi.pulse.master.vfs.provider.pulse.AbstractPulseFileObject;
import com.zutubi.util.logging.Logger;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;

import java.text.Collator;
import java.util.Comparator;

/**
 * A comparator for sorting file objects with directories before files and
 * alphabetical sorting by name within each of those categories.
 */
public class DirectoryComparator implements Comparator<FileObject>
{
    private static final Logger LOG = Logger.getLogger(DirectoryComparator.class);

    private Collator c = Collator.getInstance();
    private boolean useDisplayName = true;

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
            if(useDisplayName && o1 instanceof AbstractPulseFileObject)
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

    public void setUseDisplayName(boolean useDisplayName)
    {
        this.useDisplayName = useDisplayName;
    }
}
