package com.zutubi.pulse.master.vfs.pulse;

import org.apache.commons.vfs.FileObject;

import java.util.Comparator;

/**
 * For file objects that can provide their own comparator for sorting
 * children.
 */
public interface ComparatorProvider
{
    /**
     * @return a comparator that should be used to sort children for human
     *         consumption, or null if the children should not be sorted
     */
    Comparator<FileObject> getComparator();
}
