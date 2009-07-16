package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.postprocessors.api.Feature;
import com.zutubi.util.StringUtils;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 */
public class StoredArtifact extends Entity
{
    private static final String[] INDEX_NAMES = {"index.html", "index.htm", "default.html", "default.htm"};

    private String name;

    /**
     * Files stored as part of this artifact.  A common special case is just
     * a single file.
     */
    List<StoredFileArtifact> children = new LinkedList<StoredFileArtifact>();

    /**
     * If not null, the name of the index file in this artifact.  Used for
     * browsing the artifact as a HTML report.
     */
    private String index;

    /**
     * A special case for link artifacts.  If the URL is non-null, this
     * artifact is a link to an external location.
     */
    private String url;

    public StoredArtifact()
    {
    }

    public StoredArtifact(String name)
    {
        this.name = name;
    }

    public StoredArtifact(String name, String url)
    {
        this.name = name;
        this.url = url;
    }

    public StoredArtifact(String name, StoredFileArtifact file)
    {
        this.name = name;
        this.children.add(file);
    }

    public String getName()
    {
        return name;
    }

    private void setName(String name)
    {
        this.name = name;
    }

    public void add(StoredFileArtifact child)
    {
        children.add(child);
    }

    public List<StoredFileArtifact> getChildren()
    {
        return children;
    }

    private void setChildren(List<StoredFileArtifact> children)
    {
        this.children = children;
    }

    public boolean isSingleFile()
    {
        return children.size() == 1;
    }

    public StoredFileArtifact getFile()
    {
        return children.get(0);
    }

    public boolean hasFeatures()
    {
        for (StoredFileArtifact child : children)
        {
            if (child.hasFeatures())
            {
                return true;
            }
        }

        return false;
    }

    public Iterable<Feature.Level> getLevels()
    {
        Set<Feature.Level> result = new TreeSet<Feature.Level>();
        for (StoredFileArtifact child : children)
        {
            for (Feature.Level level : child.getLevels())
            {
                result.add(level);
            }
        }

        return result;
    }

    public boolean hasMessages(Feature.Level level)
    {
        for (StoredFileArtifact child : children)
        {
            if (child.hasMessages(level))
            {
                return true;
            }
        }

        return false;
    }

    public List<PersistentFeature> getFeatures(Feature.Level level)
    {
        List<PersistentFeature> result = new LinkedList<PersistentFeature>();
        for (StoredFileArtifact child : children)
        {
            result.addAll(child.getFeatures(level));
        }

        return result;
    }

    public String trimmedPath(StoredFileArtifact artifact)
    {
        String path = artifact.getPath();
        if (path.startsWith(name))
        {
            path = path.substring(name.length());
        }

        if (path.startsWith(File.separator))
        {
            path = path.substring(1);
        }

        if (path.startsWith("/"))
        {
            path = path.substring(1);
        }

        if (path.startsWith("\\"))
        {
            path = path.substring(1);
        }

        return path;
    }

    public String getIndex()
    {
        return index;
    }

    public void setIndex(String index)
    {
        this.index = index;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    /**
     * Returns an array of all paths for files nested under the given path in
     * this artifact.  The returned paths are relative to the given path.  Note
     * that all files nested anywhere under the path, even under child
     * directories, are returned.  Directories themselves are not returned as
     * separate entries (although they can be inferred from the file paths).
     * The path may be empty to list all files.  If the path matches no files,
     * an empty array will be returned.
     *
     * @param path path of a directory to restrict the result to, may be empty
     *             to list all files
     * @return an list of paths for files in this artifact that fall under the
     *         given path
     */
    public List<String> getFileListing(String path)
    {
        String pathPrefix = StringUtils.join("/", true, true, name, path);
        if (!pathPrefix.endsWith("/"))
        {
            pathPrefix += "/";
        }

        List<String> result = new LinkedList<String>();
        for (StoredFileArtifact fileArtifact: children)
        {
            String filePath = fileArtifact.getPath();
            if (filePath.startsWith(pathPrefix))
            {
                result.add(filePath.substring(pathPrefix.length()));
            }
        }

        return result;
    }

    public StoredFileArtifact findFile(String filePath)
    {
        for (StoredFileArtifact a : children)
        {
            if (a.getPath().equals(filePath))
            {
                return a;
            }
        }

        return null;
    }

    /**
     * Retrieve a file artifact associated with this artifact.
     *
     * @param path identifying the requested file artifact.
     * 
     * @return a stored file artifact instance, or null.
     */
    public StoredFileArtifact findFileBase(String path)
    {
        // Forward slash OK: paths are normalised in these artifacts
        return findFile(name + "/" + path);
    }

    public boolean hasIndexFile()
    {
        return findIndexFile() != null;
    }

    public String findIndexFile()
    {
        if (index != null && findFileBase(index) != null)
        {
            return index;
        }

        for (String index : INDEX_NAMES)
        {
            if (findFileBase(index) != null)
            {
                return index;
            }
        }

        return null;
    }

    public boolean isLink()
    {
        return url != null;
    }
}
