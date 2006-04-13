/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.project;

import com.zutubi.pulse.core.util.FileSystemUtils;
import com.zutubi.pulse.filesystem.File;
import com.zutubi.pulse.filesystem.FileSystem;
import com.zutubi.pulse.filesystem.FileSystemException;
import com.zutubi.pulse.web.DirectoryEntry;
import com.opensymphony.util.TextUtils;

import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Helper base class for actions for browsing file systems.
 */
public abstract class AbstractBrowseDirAction extends ProjectActionSupport
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

        if (TextUtils.stringSet(path))
        {
            entries.add(new DirectoryEntry(dir.getParentFile(), ".."));
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
