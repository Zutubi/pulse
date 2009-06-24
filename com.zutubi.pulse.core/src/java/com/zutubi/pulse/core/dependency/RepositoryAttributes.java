package com.zutubi.pulse.core.dependency;

import com.zutubi.tove.type.record.PathUtils;
import static com.zutubi.util.CollectionUtils.asMap;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.Predicate;

import java.io.File;
import java.io.IOException;
import java.io.FileFilter;
import java.util.*;

/**
 * The repository attributes represents a set of key value data pairs that are stored
 * within the artifact repository. 
 */
public class RepositoryAttributes
{
    private static final String ATTRIBUTE_FILE_NAME = ".attribute";

    public static final String PROJECT_HANDLE = "projecthandle";

    /**
     * The base directory of the artifact repository.
     */
    private File base;

    public RepositoryAttributes(File base)
    {
        this.base = base;
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
            File attributeFile = getAttributeFile(path);
            if (!attributeFile.getParentFile().exists() && !attributeFile.getParentFile().mkdirs())
            {
                throw new RepositoryAttributesException("Failed to create directory: " + attributeFile.getParentFile().getAbsolutePath());
            }
            if (!attributeFile.isFile() && !attributeFile.createNewFile())
            {
                throw new RepositoryAttributesException("Failed to create file: " + attributeFile.getAbsolutePath());
            }

            Map<String,String> attributes = readAttributeFile(attributeFile);
            attributes.put(attributeName, attributeValue);
            writeAttributeFile(attributeFile, attributes);
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
            File attributeFile = getAttributeFile(path);
            Map<String, String> attributes = readAttributeFile(attributeFile);
            if (attributes.containsKey(attributeName))
            {
                attributes.remove(attributeName);
                writeAttributeFile(attributeFile, attributes);
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
            File attributeFile = getAttributeFile(path);
            Map<String, String> attributes = readAttributeFile(attributeFile);
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

    private void applyPredicate(String path, Predicate<Map<String, String>> predicate, List<String> result) throws IOException
    {
        File attributeFile = getAttributeFile(path);
        Map<String, String> attributes = readAttributeFile(attributeFile);
        if (predicate.satisfied(attributes))
        {
            result.add(path);
        }
        
        File f = new File(base, path);

        File[] directories = f.listFiles(new FileFilter()
        {
            public boolean accept(File file)
            {
                return file.isDirectory();
            }
        });
        for (File dir : directories)
        {
            String childPath = PathUtils.getPath(path, dir.getName());
            applyPredicate(childPath, predicate, result);
        }
    }

    private Map<String, String> internalGetMergedAttributes(String path) throws IOException
    {
        if (path == null)
        {
            return new HashMap<String, String>();
        }

        Map<String, String> attributes = internalGetMergedAttributes(PathUtils.getParentPath(path));
        File attributeFile = getAttributeFile(path);
        attributes.putAll(readAttributeFile(attributeFile));
        return attributes;
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

    private synchronized Map<String, String> readAttributeFile(File f) throws IOException
    {
        if (f.isFile())
        {
            return asMap(IOUtils.read(f));
        }
        return new HashMap<String, String>();
    }

    private synchronized void writeAttributeFile(File f, Map<String, String> attributes) throws IOException
    {
        Properties props = new Properties();
        props.putAll(attributes);
        IOUtils.write(props, f);
    }
}
