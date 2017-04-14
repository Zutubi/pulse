/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
