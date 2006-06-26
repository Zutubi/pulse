package com.zutubi.pulse.web.fs;

import com.opensymphony.util.TextUtils;
import org.apache.commons.codec.binary.Base64;

import java.util.*;
import java.io.File;
import java.io.FileFilter;
import java.text.Collator;

/**
 * <class-comment/>
 */
public abstract class ListAction extends FileSystemActionSupport
{
    private List<Object> listings;

    private String[] pids;

    private boolean dirOnly = false;

    public void setDirOnly(boolean dirOnly)
    {
        this.dirOnly = dirOnly;
    }

    public boolean isDirOnly()
    {
        return this.dirOnly;
    }

    public void setPid(String[] pids)
    {
        this.pids = pids;
    }

    public List<Object> getListings()
    {
        return listings;
    }

    public String execute() throws Exception
    {
        listings = new LinkedList<Object>();
        for (String uid : pids)
        {
            listings.add(new JsonListingWrapper(list(uid)));
        }
        return SUCCESS;
    }

    private boolean isRoot(File f)
    {
        return f.isAbsolute() && !TextUtils.stringSet(f.getName());
    }

    private Listing list(String encodedPath)
    {
        //todo: validate path.
        File file = null;
        StringTokenizer tokens = new StringTokenizer(encodedPath, "/", false);
        while (tokens.hasMoreTokens())
        {
            String t = tokens.nextToken();
            if (file == null)
            {
                file = new java.io.File(decode(t));
            }
            else
            {
                file = new java.io.File(file, decode(t));
            }
        }

        Listing listing = new Listing();
        listing.path = encodedPath;

        if (file != null)
        {
            listing.files = file.listFiles();
        }
        else
        {
            listing.files = File.listRoots();
        }

        filter(listing, new FileFilter()
        {
            public boolean accept(File f)
            {
                return !f.isHidden();
            }
        });

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

    private String encode(String uid)
    {
        return new String(Base64.encodeBase64(uid.getBytes()));
    }

    private String decode(String encodedUid)
    {
        return new String(Base64.decodeBase64(encodedUid.getBytes()));
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
     * A wrapper that converts a file listing into a format used by the json response.
     *
     */
    public class JsonListingWrapper
    {
        private String uid;

        private List<JsonFileWrapper> files;

        public JsonListingWrapper(Listing l)
        {
            uid = l.path;
            files = new LinkedList<JsonFileWrapper>();
            if (l.files != null)
            {
                for (File f : l.files)
                {
                    files.add(new JsonFileWrapper(f));
                }
            }
        }

        public List<JsonFileWrapper> getListing()
        {
            return files;
        }

        public String getUid()
        {
            return uid;
        }
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

        public String getUid()
        {
            return encode(this.file.getPath());
        }

        public String getFid()
        {
            return encode(this.getName());
        }

        public String getType()
        {
            if (file.isDirectory() || isRoot(file))
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
