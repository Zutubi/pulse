package com.zutubi.pulse.web.fs;

import com.opensymphony.util.TextUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * <class-comment/>
 */
public class LsAction extends FileSystemActionSupport
{
    private List<Object> listing;

    private String path;

    private boolean dirOnly = false;

    public void setDirOnly(boolean dirOnly)
    {
        this.dirOnly = dirOnly;
    }

    public boolean isDirOnly()
    {
        return this.dirOnly;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public List<Object> getListing()
    {
        return listing;
    }

    public String execute() throws Exception
    {
        listing = new LinkedList<Object>();
        File[] files = list(path);
        if (files != null)
        {
            for (File f : files)
            {
                listing.add(new JsonFileWrapper(f));
            }
        }
        return SUCCESS;
    }

    private File[] list(String path)
    {
        //todo: validate path.
        File[] files;
        if (TextUtils.stringSet(path))
        {
            File file = new File(path);
            FileFilter filter;
            if (isDirOnly())
            {
                filter = new FileFilterChain(
                        new DirectoryOnlyFilter(),
                        new HiddenFileFilter()
                );
            }
            else
            {
                filter = new HiddenFileFilter();
            }
            files = file.listFiles(filter);
        }
        else
        {
            files = File.listRoots();
        }

        if (files != null)
        {
            Collections.sort(Arrays.asList(files), new DirectoryComparator());
        }

        return files;
    }
}
