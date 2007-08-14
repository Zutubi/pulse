package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.model.Revision;

import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 */
public abstract class CachingScmClient implements ScmClient, ScmCachePopulator
{
    public String getUniqueLocation() throws ScmException
    {
        return getLocation();
    }

    public boolean requiresRefresh(Revision revision) throws ScmException
    {
        List<Revision> newRevisions = getRevisions(revision, null);
        return newRevisions.size() > 0;
    }
/*

    public ScmFile getFile(String path) throws ScmException
    {
        Map<String, CachingScmFile> cachedListing = ScmFileCache.getInstance().lookup(this);
        if (cachedListing.containsKey(path))
        {
            return cachedListing.get(path);
        }
        else
        {
            throw new ScmException("Path '" + path + "' does not exist");
        }
    }
*/

    public List<ScmFile> browse(String path) throws ScmException
    {
        Map<String, CachingScmFile> cachedListing = ScmFileCache.getInstance().lookup(this);
        if (cachedListing.containsKey(path))
        {
            return cachedListing.get(path).list();
        }
        else
        {
            throw new ScmException("Path '" + path + "' does not exist");
        }
    }

    protected void addToCache(String filename, CachingScmFile rootFile, ScmFileCache.CacheItem item)
    {
        StringTokenizer tokens = new StringTokenizer(filename, "/", false);
        String path = "";
        CachingScmFile parent = rootFile;
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
                CachingScmFile f = new CachingScmFile(name, tokens.hasMoreTokens(), parent, path);
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
