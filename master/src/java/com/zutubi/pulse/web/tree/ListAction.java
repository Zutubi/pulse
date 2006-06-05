package com.zutubi.pulse.web.tree;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.web.ActionSupport;
import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * <class-comment/>
 */
public class ListAction extends ActionSupport
{
    private String path;
    private String encodedPath;
    private String parentPath;

    private List<Listing> results;

    public String getEncodedPath()
    {
        return encodedPath;
    }

    public void setEncodedPath(String encodedPath)
    {
        this.encodedPath = encodedPath;
    }

    public List<Listing> getResults()
    {
        return results;
    }

    public String getParentPath()
    {
        return parentPath;
    }

    public String getPath()
    {
        return path;
    }

    public String execute() throws IOException
    {
        // if path is null, assume root of file system.
        // else decode the path.
        File p;
        if (TextUtils.stringSet(encodedPath))
        {
            String decodedPath = new String(Base64.decodeBase64(encodedPath.getBytes()));
            p = new File(decodedPath);
        }
        else
        {
            p = new File("/");
        }

        path = p.getCanonicalPath();
        path = path.replace("\\", "\\\\");

        if (p.getParentFile() != null)
        {
            parentPath = new String(Base64.encodeBase64(p.getParentFile().getCanonicalPath().getBytes()));
        }

        // get listing.
        results = new LinkedList<Listing>();
        File[] listing = p.listFiles();
        for (File f : listing)
        {
            Listing l = new Listing();
            l.name = f.getName();
            l.type = (f.isFile() ? "file" : "folder");
            l.id = new String(Base64.encodeBase64(f.getCanonicalPath().getBytes()));
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
}

