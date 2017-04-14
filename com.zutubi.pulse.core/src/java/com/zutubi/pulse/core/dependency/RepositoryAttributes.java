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

package com.zutubi.pulse.core.dependency;

import com.google.common.base.Predicate;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.util.io.DirectoryFileFilter;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * The repository attributes represents a set of key value data pairs that are stored
 * within the artifact repository. 
 */
public class RepositoryAttributes
{
    protected static final String ATTRIBUTE_FILE_NAME = ".attribute";

    public static final String PROJECT_HANDLE = "projecthandle";

    private final Map<String, Map<String, String>> cache = new HashMap<String, Map<String, String>>();

    /**
     * The base directory of the artifact repository.
     */
    private File base;

    public RepositoryAttributes(File base)
    {
        this.base = base;
    }

    public void init() throws IOException
    {
        initCache("", base);
    }

    private void initCache(String path, File dir) throws IOException
    {
        // CIB-3056: This intentionally only searches the base and its immediate child directories, as that is all we
        // use.  Walking the entire repository could take a long time for no practical benefit.
        readAttributes(path);

        File[] childDirectories = dir.listFiles(DirectoryFileFilter.INSTANCE);
        if (childDirectories != null)
        {
            for (File childDirectory : childDirectories)
            {
                readAttributes(PathUtils.getPath(path, childDirectory.getName()));
            }
        }
    }

    /**
     * Get a merged map of all of the attributes located in each of the nodes of the
     * specified path.    
     *
     * Attributes defined later in the path will override those defined
     * earlier in the path.
     *
     * @param path  the path along which to merge the attributes.
     * @return  a map of attributes.
     */
    public Map<String, String> getMergedAttributes(String path)
    {
        try
        {
            return internalGetMergedAttributes(path);
        }
        catch (IOException e)
        {
            throw new RepositoryAttributesException("Internal failure: " + e.getMessage());
        }
    }

    /**
     * Add an attribute to the specified path, overwriting any existing attributes of the same
     * name that may already exist.
     *
     * @param path              the path at to which the attribute is written
     * @param attributeName     the name of the attribute
     * @param attributeValue    the value of the attribute
     */
    public void addAttribute(String path, String attributeName, String attributeValue)
    {
        try
        {
            Map<String,String> attributes = readAttributes(path);
            attributes.put(attributeName, attributeValue);
            writeAttributes(path, attributes);
        }
        catch (IOException e)
        {
            throw new RepositoryAttributesException("Internal failure: " + e.getMessage());
        }
    }

    /**
     * Remove the named attribute from the specified path.
     *
     * @param path              the path at which the attribute is expected
     * @param attributeName     the name of the attribute
     * @return  true if an attribute was located and removed, false otherwise
     */
    public boolean removeAttribute(String path, String attributeName)
    {
        try
        {
            Map<String, String> attributes = readAttributes(path);
            if (attributes.containsKey(attributeName))
            {
                attributes.remove(attributeName);

                writeAttributes(path, attributes);
                return true;
            }
            return false;
        }
        catch (IOException e)
        {
            throw new RepositoryAttributesException("Internal failure: " + e.getMessage());
        }
    }

    /**
     * Get the named attributes value from the specified path.
     * @param path              the path at which the attribute is expected
     * @param attributeName     the name of the attribute
     * @return  the value of the attribute if it is found, null otherwise
     */
    public String getAttribute(String path, String attributeName)
    {
        try
        {
            Map<String, String> attributes = readAttributes(path);
            return attributes.get(attributeName);
        }
        catch (IOException e)
        {
            throw new RepositoryAttributesException("Internal failure: " + e.getMessage());
        }
    }

    /**
     * Retrieve all of the attribute paths that match the specified predicate.
     *
     * @param predicate     the predicate to be matched
     * @return  the list of paths that match the predicate
     */
    public List<String> getPaths(Predicate<Map<String, String>> predicate)
    {
        // depth first search of repository directories.
        try
        {
            LinkedList<String> result = new LinkedList<String>();
            applyPredicate("", predicate, result);
            return result;
        }
        catch (IOException e)
        {
            throw new RepositoryAttributesException("Internal failure: " + e.getMessage()); 
        }
    }

    private synchronized void applyPredicate(String path, Predicate<Map<String, String>> predicate, List<String> result) throws IOException
    {
        for (String key : cache.keySet())
        {
            if (PathUtils.prefixPatternMatchesPath(path, key))
            {
                Map<String, String> attributes = cache.get(key);
                if (predicate.apply(attributes))
                {
                    result.add(key);
                }
            }
        }
    }

    private Map<String, String> internalGetMergedAttributes(String path) throws IOException
    {
        if (path == null)
        {
            return new HashMap<String, String>();
        }

        Map<String, String> attributes = internalGetMergedAttributes(PathUtils.getParentPath(path));

        attributes.putAll(readAttributes(path));

        return attributes;
    }

    private synchronized Map<String, String> readAttributes(String path) throws IOException
    {
        if (!cache.containsKey(path))
        {
            Map<String, String> map = new HashMap<String, String>();
            File f = getAttributeFile(path);
            if (f.isFile())
            {
                Properties properties = IOUtils.read(f);
                for (Object propertyName : properties.keySet())
                {
                    map.put((String) propertyName, properties.getProperty((String) propertyName));
                }
            }

            cache.put(path, map);
        }
        return cache.get(path);
    }

    private synchronized void writeAttributes(String path, Map<String, String> attributes) throws IOException
    {
        // update cache.
        cache.put(path, attributes);

        // update persistent file.
        File attributeFile = getAttributeFile(path);
        if (!attributeFile.getParentFile().exists() && !attributeFile.getParentFile().mkdirs())
        {
            throw new RepositoryAttributesException("Failed to create directory: " + attributeFile.getParentFile().getAbsolutePath());
        }
        if (!attributeFile.isFile() && !attributeFile.createNewFile())
        {
            throw new RepositoryAttributesException("Failed to create file: " + attributeFile.getAbsolutePath());
        }

        Properties props = new Properties();
        props.putAll(attributes);
        IOUtils.write(props, attributeFile);
    }

    private File getAttributeFile(String path)
    {
        if (path == null)
        {
            path = "";
        }
        else if (path.length() > 0)
        {
            path = path + "/";
        }

        return new File(base, path + ATTRIBUTE_FILE_NAME);
    }
}
