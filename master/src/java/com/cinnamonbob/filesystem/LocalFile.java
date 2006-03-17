package com.cinnamonbob.filesystem;

/**
 * <class-comment/>
 */
public class LocalFile implements Comparable
{
    protected final java.io.File file;
    protected final LocalFileSystem fileSystem;

    protected LocalFile(LocalFileSystem fileSystem, java.io.File file)
    {
        this.file = file;
        this.fileSystem = fileSystem;
    }

    public boolean isDirectory()
    {
        return file.isDirectory();
    }

    public boolean isFile()
    {
        return file.isFile();
    }

    public LocalFile getParentFile()
    {
        return new LocalFile(fileSystem, file.getParentFile());
    }

    public String getMimeType() throws FileNotFoundException
    {
        return fileSystem.getMimeType(this);
    }

    public long length()
    {
        return file.length();
    }

    public String getName()
    {
        return file.getName();
    }

    public int compareTo(Object o)
    {
        return file.compareTo(((LocalFile)o).file);
    }
}
