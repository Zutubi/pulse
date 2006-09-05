package com.zutubi.pulse.personal;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.zutubi.pulse.core.PulseException;
import com.zutubi.pulse.scm.FileStatus;
import com.zutubi.pulse.scm.WorkingCopy;
import com.zutubi.pulse.scm.WorkingCopyFactory;
import com.zutubi.pulse.scm.WorkingCopyStatus;
import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.util.NullOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 */
public class PatchArchive
{
    private static final String META_ENTRY = "meta.xml";

    private File patchFile;
    private static final String FILES_PATH = "files/";
    private WorkingCopyStatus status;

    /**
     * Creates a new archive from a working copy at base.  The state of the
     * working copy is established and the patch created based on that state.
     *
     * @param base      the base directory for the working copy
     * @param patchFile the destination of the patch file created
     * @throws PulseException in the event of any error creating the patch
     */
    public PatchArchive(File base, File patchFile) throws PulseException
    {
        this.patchFile = patchFile;

        WorkingCopy wc = WorkingCopyFactory.create(base);
        if(wc == null)
        {
            throw new PulseException("Unable to identify working copy '" + base.getAbsolutePath() + "'");
        }

        status = wc.getStatus();
        if(!status.inConsistentState())
        {
            // TODO dev-personal: more info!!
            throw new PulseException("Working copy '" + base.getAbsolutePath() + "' is not in a consistent state");
        }

        try
        {
            createPatchArchive(base);
        }
        catch (IOException e)
        {
            throw new PulseException("I/O error creating patch file: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a patch from an existing archive file.
     *
     * @param patchFile the file to create the patch from
     */
    public PatchArchive(File patchFile) throws PulseException
    {
        this.patchFile = patchFile;

        ZipInputStream zin = null;
        try
        {
            zin = new ZipInputStream(new FileInputStream(patchFile));
            ZipEntry entry = zin.getNextEntry();
            if(entry == null || !entry.getName().equals(META_ENTRY))
            {
                throw new PulseException("Missing meta entry in patch file '" + patchFile.getAbsolutePath() + "'");
            }

            XStream xstream = new XStream(new DomDriver());
            status = (WorkingCopyStatus) xstream.fromXML(zin);
        }
        catch(IOException e)
        {
            throw new PulseException("I/O error reading status from patch file '" + patchFile.getAbsolutePath() + "': " + e.getMessage(), e);
        }
        finally
        {
            IOUtils.close(zin);
        }
    }

    private void createPatchArchive(File base) throws IOException
    {
        // The zip archive is laid out as follows:
        // <root>/
        //     meta.xml: information about the patch: revision and change info for files
        //     files/
        //         <changed files>: all files that have been added/edited, laid out in
        //                          the same directory structure as the working copy

        ZipOutputStream os = null;

        try
        {
            os = new ZipOutputStream(new FileOutputStream(patchFile));
            addMeta(os);
            addFiles(base, os);
        }
        finally
        {
            IOUtils.close(os);
        }
    }

    private void addMeta(ZipOutputStream os) throws IOException
    {
        ZipEntry entry = new ZipEntry("meta.xml");
        os.putNextEntry(entry);
        XStream xstream = new XStream(new DomDriver());
        xstream.toXML(status, os);
    }

    private void addFiles(File base, ZipOutputStream os) throws IOException
    {
        os.putNextEntry(new ZipEntry(FILES_PATH));
        for(FileStatus fs: status)
        {
            if (fs.getState().requiresFile() && !fs.isDirectory())
            {
                File f = new File(base, FileSystemUtils.denormaliseSeparators(fs.getPath()));
                String path = FILES_PATH + fs.getPath();
                addFile(os, f, path);
            }
        }
    }

    private void addFile(ZipOutputStream os, File f, String path) throws IOException
    {
        ZipEntry entry = new ZipEntry(path);
        entry.setTime(f.lastModified());
        os.putNextEntry(entry);
        FileInputStream is = null;

        try
        {
            is = new FileInputStream(f);
            IOUtils.joinStreams(is, os);
        }
        finally
        {
            IOUtils.close(is);
        }
    }

    public File getPatchFile()
    {
        return patchFile;
    }

    public WorkingCopyStatus getStatus()
    {
        return status;
    }

    public void apply(File base) throws PulseException
    {
        try
        {
            for(FileStatus fs: status)
            {
                fs.apply(base);
            }

            unzip(base);
        }
        catch (IOException e)
        {
            throw new PulseException("I/O error applying patch file '" + patchFile.getAbsolutePath() + "': " + e.getMessage(), e);
        }
    }

    private void unzip(File base) throws IOException
    {
        ZipInputStream zin = null;

        try
        {
            zin = new ZipInputStream(new FileInputStream(patchFile));

            // Skip over meta entry
            zin.getNextEntry();
            IOUtils.joinStreams(zin, new NullOutputStream());

            ZipEntry entry;
            while((entry = zin.getNextEntry()) != null)
            {
                extractEntry(base, entry, zin);
            }
        }
        finally
        {
            IOUtils.close(zin);
        }
    }

    private void extractEntry(File base, ZipEntry entry, ZipInputStream zin) throws IOException
    {
        File f = new File(base, getPath(entry));
        if(entry.isDirectory())
        {
            f.mkdirs();
        }
        else
        {
            File parent = f.getParentFile();
            if(!parent.isDirectory())
            {
                parent.mkdirs();
            }

            FileOutputStream out = null;
            try
            {
                out = new FileOutputStream(f);
                IOUtils.joinStreams(zin, out);
            }
            finally
            {
                IOUtils.close(out);
            }
        }

        f.setLastModified(entry.getTime());
    }

    private String getPath(ZipEntry entry) throws IOException
    {
        String name = entry.getName();
        if(!name.startsWith(FILES_PATH))
        {
            throw new IOException("Unexpected entry path '" + name + "': should start with '" + FILES_PATH + "'");
        }

        return name.substring(FILES_PATH.length());
    }

    public boolean containsPath(String path)
    {
        for(FileStatus fs: status)
        {
            if(fs.getPath().equals(path) && fs.getState().requiresFile() == true)
            {
                return true;
            }
        }

        return false;
    }

    public String retrieveFile(String path) throws IOException
    {
        path = FILES_PATH + path;
        
        ZipInputStream zin = null;
        try
        {
            zin = new ZipInputStream(new FileInputStream(patchFile));
            ZipEntry entry;
            while((entry = zin.getNextEntry()) != null)
            {
                if(entry.getName().equals(path))
                {
                    // This is it
                    return IOUtils.inputStreamToString(zin);
                }
            }

            throw new IOException("Path '" + path + "' not found in archive");
        }
        finally
        {
            IOUtils.close(zin);
        }
    }
}
