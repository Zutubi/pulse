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
    private String[] uids;

    private List<Carrier> results;

    /**
     * Setter for the UID property.
     *
     * @param uids
     */
    public void setUid(String[] uids)
    {
        this.uids = uids;
    }

    public List<Carrier> getResults()
    {
        return results;
    }

    public String execute() throws IOException, FileSystemException
    {
        results = new LinkedList<Carrier>();
        for (String uid : uids)
        {
            results.add(generateListing(uid));
        }

        return SUCCESS;
    }

    private Carrier generateListing(String uid) throws FileSystemException
    {
        FileSystem fileSystem = getFileSystem();

        // if path is null, assume root of file system.
        // else decode the path.
        
        File path = fileSystem.getFile(""); // filesystem default.
        if (TextUtils.stringSet(uid))
        {
            String decodedPath = decode(uid);
            path = fileSystem.getFile(decodedPath);
        }

        // get listing.
        List<Listing> res = new LinkedList<Listing>();
        File[] listing = fileSystem.list(path);
        for (File f : listing)
        {
            Listing l = new Listing();
            l.name = f.getName();
            l.type = (f.isFile() ? "file" : "folder");
            l.uid = encode(f.getPath());
            res.add(l);
        }

        // sort, directories first.
        Collections.sort(res, new Comparator<Listing>()
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
        Carrier c = new Carrier();
        c.listing = res;
        c.path = path.getPath();
        c.uid = uid;
        return c;
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

        public List<Listing> getListing()
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

