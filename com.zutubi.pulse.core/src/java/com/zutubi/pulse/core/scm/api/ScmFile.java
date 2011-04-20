package com.zutubi.pulse.core.scm.api;

import com.zutubi.util.FileSystemUtils;

/**
 * Represents a file or directory stored in an SCM.
 */
public class ScmFile implements Comparable<ScmFile>
{
    public static String SEPARATOR = FileSystemUtils.NORMAL_SEPARATOR;

    private String path;
    private boolean directory = false;

    /**
     * Creates a regular SCM file (i.e. not a directory) for a given path.
     *
     * @param path path of the file, relative to the location specified for the
     *             SCM client
     */
    public ScmFile(String path)
    {
        this(path, false);
    }

    /**
     * Creates an SCM file for a given path, indicating if it is a directory.
     *
     * @param path path of the file, relative to the location specified for the
     *             SCM client
     * @param dir  if true, this path denotes a directory
     */
    public ScmFile(String path, boolean dir)
    {
        this.path = normalisePath(path);
        this.directory = dir;
    }

    /**
     * Creates a regular SCM file (i.e. not a directory) for a given parent
     * path and file name.
     *
     * @param parent parent path for the file, relative to the location
     *               specified for the SCM client
     * @param name   name of the file (last component of its full path)
     */
    public ScmFile(String parent, String name)
    {
        this(parent, name, false);
    }

    /**
     * Creates an SCM file for a given parent path and file name, indicating if
     * it is a directory.
     *
     * @param parent parent path for the file, relative to the location
     *               specified for the SCM client
     * @param name   name of the file (last component of its full path)
     * @param dir  if true, this path denotes a directory
     */
    public ScmFile(String parent, String name, boolean dir)
    {
        this(normalisePath(parent) + SEPARATOR + normalisePath(name), dir);
    }

    /**
     * Creates a regular SCM file (i.e. not a directory) for a given parent
     * file and file name.
     *
     * @param parent parent file, the directory under which this file is nested
     * @param name   name of the file (last component of its full path)
     */
    public ScmFile(ScmFile parent, String name)
    {
        this(parent, name, false);
    }

    /**
     * Creates an SCM file for a given parent file and file name, indicating if
     * it is a directory.
     *
     * @param parent parent file, the directory under which this file is nested
     * @param name   name of the file (last component of its full path)
     * @param dir    if true, this path denotes a directory
     */
    public ScmFile(ScmFile parent, String name, boolean dir)
    {
        this(parent.getPath() + SEPARATOR + normalisePath(name), dir);
    }

    /**
     * @return the path of this file, relative to the location specified to
     *         create the corresponding SCM client.  Note all separators are
     *         normalised to {@link #SEPARATOR}.
     * @see #SEPARATOR
     */
    public String getPath()
    {
        return path;
    }

    /**
     * @return the name of this file, which is the last component of the path
     */
    public String getName()
    {
        int lastIndex = path.lastIndexOf(SEPARATOR);
        if (lastIndex != -1)
        {
            return path.substring(lastIndex + 1);
        }
        return this.path;
    }

    /**
     * @return the parent path, or null if this file has no parent
     * @see #getParentFile()
     */
    public String getParent()
    {
        int index = path.lastIndexOf(SEPARATOR);
        if (index < 0)
        {
            return null;
        }
        return path.substring(0, index);
    }

    /**
     * @return a file created for the parent path, or null if this file has no
     *         parent
     * @see #getParent()
     */
    public ScmFile getParentFile()
    {
        String p = this.getParent();
        if (p == null)
        {
            return null;
        }
        return new ScmFile(p, true);
    }

    /**
     * @return true iff this path denotes a directory
     * @see #isFile()
     */
    public boolean isDirectory()
    {
        return directory;
    }

    /**
     * @return true iff this path denotes a regular file (i.e. not a directory)
     * @see #isDirectory()
     */
    public boolean isFile()
    {
        return !isDirectory();
    }

    private static String normalisePath(String path)
    {
        path = FileSystemUtils.normaliseSeparators(path);
        if (path.startsWith(SEPARATOR))
        {
            path = path.substring(1);
        }
        if (path.endsWith(SEPARATOR))
        {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    // auto-generated.

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScmFile file = (ScmFile) o;

        if (directory != file.directory) return false;
        return !(path != null ? !path.equals(file.path) : file.path != null);
    }

    public int hashCode()
    {
        int result;
        result = (path != null ? path.hashCode() : 0);
        result = 31 * result + (directory ? 1 : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return path + (isDirectory() ? "/" : "");
    }

    /**
     * Compares two files by their name only, so siblings can be sorted.
     *
     * @param other file to compare to
     * @return {@inheritDoc}
     */
    public int compareTo(ScmFile other)
    {
        return getName().compareTo(other.getName());
    }
}
