package com.zutubi.pulse.master.vfs.provider.pulse.reference;

import com.google.common.base.Function;
import com.zutubi.pulse.master.vfs.provider.pulse.AbstractPulseFileObject;
import com.zutubi.pulse.servercore.bootstrap.SystemPaths;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Sort;
import com.zutubi.util.io.FileSystemUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;

/**
 * Abstract base for all reference doc files.  Handles common behaviour such as
 * listing of static pages.
 */
public abstract class AbstractReferenceFileObject extends AbstractPulseFileObject
{
    private String[] staticChildren;
    private SystemPaths systemPaths;
    private static final String SUFFIX_VELOCITY = ".vm";

    /**
     * Creates a new reference file object.
     *
     * @param name the name of this file object instance.
     * @param fs   the filesystem this file belongs to.
     */
    public AbstractReferenceFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    @Override
    protected String[] doListChildren() throws Exception
    {
        String[] staticChildren = getStaticChildren();
        String[] dynamicChildren = getDynamicChildren();

        if (staticChildren.length == 0)
        {
            // Optimise this common case.
            return dynamicChildren;
        }
        else
        {
            List<String> allChildren = newArrayList(concat(asList(staticChildren), asList(dynamicChildren)));
            Collections.sort(allChildren, new Sort.StringComparator());
            return allChildren.toArray(new String[allChildren.size()]);
        }
    }

    public String getStaticPath() throws FileSystemException
    {
        ReferenceRootFileObject rootFile = getAncestor(ReferenceRootFileObject.class);
        return rootFile.getName().getRelativeName(getName());
    }

    protected String[] getStaticChildren() throws FileSystemException
    {
        if (staticChildren == null)
        {
            File staticRoot = new File(systemPaths.getContentRoot(), FileSystemUtils.composeFilename("ajax", "reference", "static"));
            File candidateDir = new File(staticRoot, getStaticPath());
            if (candidateDir.isDirectory())
            {
                String[] templates = candidateDir.list(new SuffixFileFilter(SUFFIX_VELOCITY));
                staticChildren = transform(asList(templates), new Function<String, String>()
                {
                    public String apply(String s)
                    {
                        return s.substring(0, s.length() - SUFFIX_VELOCITY.length());
                    }
                }).toArray(new String[templates.length]);
            }
            else
            {
                staticChildren = new String[0];
            }
        }

        return staticChildren;
    }

    @Override
    public AbstractPulseFileObject createFile(FileName fileName) throws FileSystemException
    {
        if (CollectionUtils.contains(getStaticChildren(), fileName.getBaseName()))
        {
            return createStaticFile(fileName);
        }
        else
        {
            return createDynamicFile(fileName);
        }
    }

    @Override
    protected FileType doGetType() throws Exception
    {
        return doListChildren().length == 0 ? FileType.FILE : FileType.FOLDER;
    }

    protected AbstractPulseFileObject createStaticFile(FileName fileName)
    {
        return objectFactory.buildBean(StaticReferenceFileObject.class, fileName, pfs);
    }

    /**
     * Returns an array of all dynamic child file names.  These are any child
     * files that are not static pages layed out on disk.
     *
     * @return the names of all dynamic children
     * @throws FileSystemException on any error
     */
    protected abstract String[] getDynamicChildren() throws FileSystemException;

    /**
     * Creates a dynamic child file.
     *
     * @param fileName name of the file to create
     * @return the created file
     * 
     * @see #getDynamicChildren()
     */
    protected abstract AbstractPulseFileObject createDynamicFile(FileName fileName);

    public void setSystemPaths(SystemPaths systemPaths)
    {
        this.systemPaths = systemPaths;
    }
}
