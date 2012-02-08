package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.scm.api.*;

import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 */
public abstract class CachingScmClient implements ScmClient, ScmCachePopulator
{
    public String getUniqueLocation(ScmContext context) throws ScmException
    {
        return getLocation(context);
    }

    public boolean requiresRefresh(ScmContext context, Revision revision) throws ScmException
    {
        List<Revision> newRevisions = getRevisions(context, revision, null);
        return newRevisions.size() > 0;
    }

    public List<ScmFile> browse(ScmContext context, String path, Revision revision) throws ScmException
    {
        Map<String, CachingScmFile> cachedListing = ScmFileCache.getInstance().lookup(context, this);
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
                CachingScmFile f = new CachingScmFile(path, tokens.hasMoreTokens());
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
