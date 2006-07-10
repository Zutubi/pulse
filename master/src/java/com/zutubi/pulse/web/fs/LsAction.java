package com.zutubi.pulse.web.fs;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.web.ActionSupport;

import java.io.File;
import java.io.FileFilter;
import java.text.Collator;
import java.util.*;

/**
 * <class-comment/>
 */
public class LsAction extends ActionSupport
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
        Listing ls = list(path);
        if (ls != null && ls.files != null)
        {
            for (File f : ls.files)
            {
                listing.add(new JsonFileWrapper(f));
            }
        }
        return SUCCESS;
    }

    private boolean isRoot(File f)
    {
        return f.isAbsolute() && !TextUtils.stringSet(f.getName());
    }

    private Listing list(String path)
    {
        //todo: validate path.
        File file = null;
        if (TextUtils.stringSet(path))
        {
            file = new File(path);
        }

        Listing listing = new Listing();
        listing.path = path;

        if (file != null)
        {
            listing.files = file.listFiles();

            filter(listing, new FileFilter()
            {
                public boolean accept(File f)
                {
                    return !f.isHidden();
                }
            });
        }
        else
        {
            listing.files = File.listRoots();
        }

        if (isDirOnly())
        {
            filter(listing, new FileFilter()
            {
                public boolean accept(File f)
                {
                    return f.isDirectory();
                }
            });
        }

        sort(listing.files);

        return listing;
    }

    private void filter(Listing listing, FileFilter filter)
    {
        if (listing.files != null)
        {
            List<File> filtered = new LinkedList<File>();
            for (File f : listing.files)
            {
                if (filter.accept(f))
                {
                    filtered.add(f);
                }
            }
            listing.files = filtered.toArray(new File[filtered.size()]);
        }
    }

    private void sort(File[] files)
    {
        final Collator c = Collator.getInstance();
        if (files != null)
        {
            Collections.sort(Arrays.asList(files), new Comparator<File>()
            {
                public int compare(File o1, File o2)
                {
                    // folders first.
                    if ((o1.isDirectory() || isRoot(o1)) && (!o2.isDirectory() && !isRoot(o2)))
                    {
                        return -1;
                    }
                    if ((o2.isDirectory() || isRoot(o2)) && (!o1.isDirectory() && !isRoot(o1)))
                    {
                        return 1;
                    }
                    // then sort alphabetically
                    return c.compare(o1.getAbsolutePath(), o2.getAbsolutePath());
                }
            });
        }
    }

    /**
     * A data carrier object that holds information about a file system listing.
     *
     */
    public class Listing
    {
        /**
         * The listing argument: which path.
         */
        String path;

        /**
         * The listing result: the files located at the specified path.
         */
        File[] files;
    }

    /**
     * A wrapper that converts file objects into the format used by the json response.
     */
    public class JsonFileWrapper
    {
        private final File file;

        public JsonFileWrapper(File f)
        {
            this.file = f;
        }

        public String getName()
        {
            if (isRoot(this.file))
            {
                return this.file.getAbsolutePath();
            }
            return this.file.getName();
        }

        public String getSeparator()
        {
            return File.separator;
        }

        public String getType()
        {
            if (isRoot(file))
            {
                return "root";
            }
            else if (file.isDirectory())
            {
                return "folder";
            }
            else
            {
                return "file";
            }
        }
    }
}
