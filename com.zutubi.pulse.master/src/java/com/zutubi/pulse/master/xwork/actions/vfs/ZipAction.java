package com.zutubi.pulse.master.xwork.actions.vfs;

import com.zutubi.pulse.core.util.PulseZipUtils;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.vfs.provider.pulse.AbstractPulseFileObject;
import com.zutubi.util.RandomUtils;
import com.zutubi.util.StringUtils;
import com.zutubi.util.io.TempFileInputStream;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * <class comment/>
 */
public class ZipAction extends VFSActionSupport
{
    /**
     * @deprecated  use path instead.
     */
    private String root;

    private String path;

    private InputStream inputStream;

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

    public long getContentLength()
    {
        return contentLength;
    }

    /**
     * @deprecated
     */
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
        if (StringUtils.stringSet(root))
        {
            path = root + path;
        }
        
        FileObject fo = getFS().resolveFile(path);

        AbstractPulseFileObject pfo = (AbstractPulseFileObject) fo;
        File base = pfo.toFile();
        if (base == null || !base.exists())
        {
            addActionError("The requested file does not exist: " + path);
            return ERROR;
        }

        File tmpRoot = configurationManager.getSystemPaths().getTmpRoot();
        if (!tmpRoot.exists() && !tmpRoot.mkdirs())
        {
            addActionError("Failed to create pulse temporary directory: " + tmpRoot.getAbsolutePath());
            return ERROR;
        }

        File temp = new File(tmpRoot, RandomUtils.insecureRandomString(7) + ".zip");

        try
        {
            PulseZipUtils.createZip(temp, base.getParentFile(), base.getName());
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
