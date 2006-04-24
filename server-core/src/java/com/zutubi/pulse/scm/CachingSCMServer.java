/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.scm;

import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.filesystem.remote.CachingRemoteFile;
import com.zutubi.pulse.filesystem.remote.RemoteFile;

import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 */
public abstract class CachingSCMServer implements SCMServer, SCMCachePopulator
{
    public String getUniqueLocation()
    {
        return getLocation();
    }

    public boolean requiresRefresh(Revision revision) throws SCMException
    {
        return hasChangedSince(revision);
    }

    public RemoteFile getFile(String path) throws SCMException
    {
        Map<String, CachingRemoteFile> cachedListing = SCMFileCache.getInstance().lookup(this);
        if (cachedListing.containsKey(path))
        {
            return cachedListing.get(path);
        }
        else
        {
            throw new SCMException("Path '" + path + "' does not exist");
        }
    }

    public List<RemoteFile> getListing(String path) throws SCMException
    {
        Map<String, CachingRemoteFile> cachedListing = SCMFileCache.getInstance().lookup(this);
        if (cachedListing.containsKey(path))
        {
            return cachedListing.get(path).list();
        }
        else
        {
            throw new SCMException("Path '" + path + "' does not exist");
        }
    }

    protected void addToCache(String filename, CachingRemoteFile rootFile, SCMFileCache.CacheItem item)
    {
        StringTokenizer tokens = new StringTokenizer(filename, "/", false);
        String path = "";
        CachingRemoteFile parent = rootFile;
        while (tokens.hasMoreTokens())
        {
            String name = tokens.nextToken();
            if (path.length() > 0 && !path.endsWith("/"))
            {
                path += "/";
            }

            path += name;

            if (!item.cachedListing.containsKey(path))
            {
                CachingRemoteFile f = new CachingRemoteFile(name, tokens.hasMoreTokens(), parent, path);
                if (parent != null)
                {
                    parent.addChild(f);
                }
                item.cachedListing.put(path, f);
            }
            
            parent = item.cachedListing.get(path);
        }
    }
}
