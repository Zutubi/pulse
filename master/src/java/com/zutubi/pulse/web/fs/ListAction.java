package com.zutubi.pulse.web.fs;

import com.zutubi.pulse.filesystem.File;
import com.zutubi.pulse.filesystem.FileSystem;
import com.zutubi.pulse.filesystem.FileSystemException;
import org.apache.commons.codec.binary.Base64;

import java.util.*;

/**
 * <class-comment/>
 */
public abstract class ListAction extends FileSystemActionSupport
{
    private List<Object> listings;

    private String[] uids;

    public void setUid(String[] paths)
    {
        this.uids = paths;
    }

    public List<Object> getListings()
    {
        return listings;
    }

    public String execute() throws Exception
    {
        listings = new LinkedList<Object>();
        for (String uid : uids)
        {
            listings.add(new JsonListingWrapper(list(uid)));
        }
        return SUCCESS;
    }

    private Listing list(String encodedPath) throws FileSystemException
    {
        String decodedPath = decode(encodedPath);
        FileSystem fs = getFileSystem();

        File file = fs.getFile(decodedPath);

        //todo: validate path.
        Listing listing = new Listing();
        listing.files = fs.list(file);
        listing.path = decodedPath;

        sort(listing.files);

        return listing;
    }

    private void sort(File[] files)
    {
        Collections.sort(Arrays.asList(files), new Comparator<File>()
        {
            public int compare(File o1, File o2)
            {
                // folders first.
                if (o1.isDirectory())
                {
                    return -1;
                }
                if (o2.isDirectory())
                {
                    return 1;
                }
                return 0;
            }
        });

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

        private String path;

        private List<JsonFileWrapper> files;

        public JsonListingWrapper(Listing l)
        {
            path = l.path;
            uid = encode(l.path);
            files = new LinkedList<JsonFileWrapper>();
            for (File f : l.files)
            {
                files.add(new JsonFileWrapper(f));
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

        public String getPath()
        {
            return path;
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
            return this.file.getName();
        }

        public String getUid()
        {
            return encode(this.file.getPath());
        }

        public String getType()
        {
            if (file.isFile())
            {
                return "file";
            }
            else
            {
                return "folder";
            }
        }
    }
}
