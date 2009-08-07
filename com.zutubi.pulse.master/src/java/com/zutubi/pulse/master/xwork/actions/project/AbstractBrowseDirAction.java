package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.xwork.actions.DirectoryEntry;
import com.zutubi.pulse.servercore.filesystem.File;
import com.zutubi.pulse.servercore.filesystem.FileSystem;
import com.zutubi.pulse.servercore.filesystem.FileSystemException;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.StringUtils;

import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Helper base class for actions for browsing file systems.
 */
public abstract class AbstractBrowseDirAction extends ProjectActionBase
{
    private String path = "";
    private List<DirectoryEntry> entries;
    private InputStream inputStream;
    private String contentType;

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public List<DirectoryEntry> getEntries()
    {
        return entries;
    }

    public InputStream getInputStream()
    {
        return inputStream;
    }

    public String getContentType()
    {
        return contentType;
    }

    public String getDisplayPath()
    {
        String result = FileSystemUtils.normaliseSeparators(path);
        if (result.startsWith("/"))
        {
            result = result.substring(1);
        }

        return result;
    }

    public boolean getShowSizes()
    {
        return true;
    }

    private void createDirectoryEntries(FileSystem fs, File dir) throws FileSystemException
    {
        entries = new LinkedList<DirectoryEntry>();

        if (StringUtils.stringSet(path))
        {
            File parentFile = dir.getParentFile();
            entries.add(new DirectoryEntry(parentFile, ".."));
        }

        File[] files = fs.list(dir);
        Arrays.sort(files);
        for (File f : files)
        {
            entries.add(new DirectoryEntry(f, f.getName()));
        }
    }

    public String execute(FileSystem fs)
    {
        try
        {
            File file = fs.getFile(path);

            if (file.isDirectory())
            {
                createDirectoryEntries(fs, file);
                return "dir";
            }
            else if (file.isFile())
            {
                inputStream = fs.getFileContents(file);
                contentType = fs.getMimeType(file);
                return "file";
            }
        }
        catch (FileSystemException fse)
        {
            addActionError(fse.getMessage());
            return ERROR;
        }

        addActionError("Path '" + path + "' does not exist");
        return ERROR;
    }
}
