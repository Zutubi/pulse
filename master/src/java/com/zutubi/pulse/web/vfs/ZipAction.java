package com.zutubi.pulse.web.vfs;

import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileObject;

import java.io.InputStream;
import java.io.File;
import java.io.IOException;

import com.zutubi.pulse.util.RandomUtils;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.TempFileInputStream;
import com.zutubi.pulse.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.vfs.pulse.PulseFileObject;

/**
 * <class comment/>
 */
public class ZipAction extends VFSActionSupport
{
    private String root;
    private String path;

    private InputStream inputStream;

    private String contentType;

    private String filename;
    private MasterConfigurationManager configurationManager;
    private long contentLength;

    public String getFilename()
    {
        return filename;
    }

    public InputStream getInputStream()
    {
        return inputStream;
    }

    public String getContentType()
    {
        return contentType;
    }

    public long getContentLength()
    {
        return contentLength;
    }

    public void setRoot(String root)
    {
        this.root = root;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String execute() throws FileSystemException
    {
        FileObject fo = getFS().resolveFile(root + path);

        PulseFileObject pfo = (PulseFileObject) fo;
        File base = pfo.getBase();

        // what if base is null or does not exist?....

        File tmpRoot = configurationManager.getSystemPaths().getTmpRoot();
        if (!tmpRoot.exists() && !tmpRoot.mkdirs())
        {
            addActionError("Failed to create pulse temporary directory: " + tmpRoot.getAbsolutePath());
            return ERROR;
        }

        File temp = new File(tmpRoot, RandomUtils.randomString(7) + ".zip");

        try
        {
            FileSystemUtils.createZip(temp, base.getParentFile(), base);
            contentLength = temp.length();
            filename = base.getName() + ".zip";
            inputStream = new TempFileInputStream(temp);
        }
        catch (IOException e)
        {
            addActionError("I/O error zipping directory artifact: " + e.getMessage());
            return ERROR;
        }
        finally
        {
            temp.delete();
        }

        return SUCCESS;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }

}
