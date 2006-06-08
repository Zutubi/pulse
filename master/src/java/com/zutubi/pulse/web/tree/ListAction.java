package com.zutubi.pulse.web.tree;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.web.ActionSupport;
import com.zutubi.pulse.filesystem.FileSystem;
import com.zutubi.pulse.filesystem.File;
import com.zutubi.pulse.filesystem.FileSystemException;
import com.zutubi.pulse.filesystem.local.LocalFileSystem;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.util.*;

/**
 * The list action is used by the tree widget to generate a listing of the children of a specific node.
 *
 */
public class ListAction extends ActionSupport
{
    /**
     * The Unique IDentifier that specifies the node that is being viewed.
     */
    private String uid;

    /**
     * A human readable representation of the uid.
     */
    private String path;

    private List<Listing> results;

    /**
     * Getter for the UID property.
     *
     * @return current node uid
     */
    public String getUid()
    {
        return uid;
    }

    /**
     * Setter for the UID property.
     *
     * @param uid
     */
    public void setUid(String uid)
    {
        this.uid = uid;
    }

    public List<Listing> getResults()
    {
        return results;
    }

    public List<Carrier> getListings()
    {
        List<Carrier> listings = new LinkedList<Carrier>();
        Carrier c = new Carrier();
        c.listing = getResults();
        c.uid = getUid();
        c.path = getPath();
        listings.add(c);
        return listings;
    }

    /**
     * Getter for the path property.
     *
     * @return a human readable representation of the UID.
     */
    public String getPath()
    {
        return path;
    }

    public String execute() throws IOException, FileSystemException
    {
        FileSystem fileSystem = getFileSystem();

        // if path is null, assume root of file system.
        // else decode the path.
        File p;
        if (TextUtils.stringSet(uid))
        {
            String decodedPath = decode(uid);
            p = fileSystem.getFile(decodedPath);
        }
        else
        {
            // filesystem default.
            p = fileSystem.getFile("");
        }

        path = p.getPath();

        // get listing.
        results = new LinkedList<Listing>();
        File[] listing = fileSystem.list(p);
        for (File f : listing)
        {
            Listing l = new Listing();
            l.name = f.getName();
            l.type = (f.isFile() ? "file" : "folder");
            l.uid = encode(f.getPath());
            results.add(l);
        }

        // sort, directories first.
        Collections.sort(results, new Comparator<Listing>()
        {
            public int compare(Listing o1, Listing o2)
            {
                // folders first.
                if (o1.type.equals("folder"))
                {
                    return -1;
                }
                if (o2.type.equals("folder"))
                {
                    return 1;
                }
                return 0;
            }
        });

        return SUCCESS;
    }

    private String encode(String uid)
    {
        return new String(Base64.encodeBase64(uid.getBytes()));
    }

    private String decode(String encodedUid)
    {
        return new String(Base64.decodeBase64(encodedUid.getBytes()));
    }

    private LocalFileSystem getFileSystem()
    {
        return new LocalFileSystem(new java.io.File("c:/"));
    }

    public class Carrier
    {
        List<Listing> listing;
        String uid;
        String path;

        public List<Listing> getResults()
        {
            return listing;
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
}

