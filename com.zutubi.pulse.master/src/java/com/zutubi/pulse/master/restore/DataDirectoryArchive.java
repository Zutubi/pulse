package com.zutubi.pulse.master.restore;

import com.zutubi.pulse.master.database.DatabaseConsole;
import com.zutubi.pulse.master.database.HSQLDBUtils;
import com.zutubi.pulse.servercore.bootstrap.MasterUserPaths;
import com.zutubi.util.FileSystemUtils;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;

/**
 * This implementation of the archiveable component interface takes a snapshot of the
 * configuration contents of the PULSE_DATA directory.
 * <ul>
 * <li>PULSE_DATA/config/* -> base/config</li>
 * <li>PULSE_DATA/pulse.config.properties -> base/pulse.config.properties</li>
 * </ul>
 */
public class DataDirectoryArchive extends AbstractArchiveableComponent
{
    private MasterUserPaths paths;
    private DatabaseConsole databaseConsole;
    private DataSource dataSource;

    public String getName()
    {
        return "config";
    }

    public String getDescription()
    {
        return "The configuration restoration takes the appropriate 1.2.x system files and restores " +
                "them to their 2.0 locations.  Note, the one system file excluded from this process is the" +
                "database.properties file.  The database configured for your 2.0 installation will be retained.";
    }

    public void backup(File archive) throws ArchiveException
    {
        try
        {
            FileSystemUtils.copy(new File(archive, "config"), paths.getUserConfigRoot());
            FileSystemUtils.copy(archive, new File(paths.getData(), "pulse.config.properties"));

            // provide some backward compatibility with the original backup process by backing up the
            // PULSE_DATA/database directory if it is being used.  When we start using the DatabaseArchive
            // for managing database backups, this can be removed.
            // Are we running an embedded database? If so, we need to back it up.
            if (databaseConsole.isEmbedded())
            {
                // trigger a checkpoint call on the database to compact the data.
                HSQLDBUtils.compactDatabase(dataSource);
                File dest = new File(archive, "database");
                dest.mkdir();
                conditionalCopy(dest, new File(paths.getDatabaseRoot(), "db.backup"));
                conditionalCopy(dest, new File(paths.getDatabaseRoot(), "db.log"));
                conditionalCopy(dest, new File(paths.getDatabaseRoot(), "db.properties"));
                conditionalCopy(dest, new File(paths.getDatabaseRoot(), "db.data"));
                conditionalCopy(dest, new File(paths.getDatabaseRoot(), "db.script"));
            }
        }
        catch (IOException e)
        {
            throw new ArchiveException(e);
        }
    }

    public void restore(File archive) throws ArchiveException
    {
        // replace the existing files with the archived files.
        try
        {
            cleanup(paths.getUserConfigRoot());

            FileSystemUtils.delete(new File(paths.getData(), "pulse.config.properties"));

            // can not use the fsu.copy since it expects the destination directory to be empty, which
            // will not always be the case.
            File configRoot = paths.getUserConfigRoot();
            if (!configRoot.isDirectory() && !configRoot.mkdirs())
            {
                throw new IOException("Failed to create directory: " + configRoot.getCanonicalPath());
            }
            copy(paths.getUserConfigRoot(), new File(archive, "config").listFiles());

            FileSystemUtils.copy(paths.getData(), new File(archive, "pulse.config.properties"));

            // since the contents of the archive/database directory contain the core files of the hsqldb,
            // we can not simply copy that into the PULSE_DATA/database directory.  The database first needs
            // to be shutdown and the files replaced by the archived files.  This is beyond the scope of this
            // archive restore and so will need to be handled manually if that case should arise.
        }
        catch (IOException e)
        {
            throw new ArchiveException(e);
        }
    }

    /**
     * An overwriting copy that will overwrite destination files that already exist unless
     * they are database configuration files. That is, the filename starts with 'database'.
     *
     * @param dest  the destination directory into which the files are copied.
     * @param files the files being copied to the destination directory
     * @throws IOException is thrown on error
     */
    private void copy(File dest, File[] files) throws IOException
    {
        for (File file : files)
        {
            if (file.isFile())
            {
                // does the file already exist? and if so, is it a database file?. If so, do not overwrite it.
                File target = new File(dest, file.getName());
                if (!target.exists())
                {
                    FileSystemUtils.copy(dest, file);
                }
                else
                {
                    if (!file.getName().startsWith("database"))
                    {
                        FileSystemUtils.copy(dest, file);
                    }
                }
            }
            else if (file.isDirectory())
            {
                File nestedDestination = new File(dest, file.getName());
                if (!nestedDestination.isDirectory() && !nestedDestination.mkdirs())
                {
                    throw new IOException("Failed to create destination directory: " + nestedDestination);
                }
                copy(nestedDestination, file.listFiles());
            }
        }
    }

    /**
     * Copy the named file to the destination if and only if the file exists.
     * @param dest  the destination of the copy
     * @param file  the source of the copy
     * @see com.zutubi.util.FileSystemUtils#copy(java.io.File, java.io.File[])
     * @throws IOException is thrown on error.
     */
    private void conditionalCopy(File dest, File file) throws IOException
    {
        if(file.exists())
        {
            FileSystemUtils.copy(dest, file);
        }
    }

    /**
     * Delete all of the files located in the specified directory unless the name of the
     * file begins with 'database'/
     *
     * @param base  the directory being cleaned up.
     * @throws IOException  is thrown on error.
     */
    private void cleanup(File base) throws IOException
    {
        if (base.exists())
        {
            for (File file : base.listFiles())
            {
                if (file.isDirectory())
                {
                    cleanup(file);
                }
                if (!file.getName().startsWith("database"))
                {
                    FileSystemUtils.delete(file);
                }
            }
        }
    }

    public void setUserPaths(MasterUserPaths paths)
    {
        this.paths = paths;
    }

    public void setDatabaseConsole(DatabaseConsole databaseConsole)
    {
        this.databaseConsole = databaseConsole;
    }

    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }
}
