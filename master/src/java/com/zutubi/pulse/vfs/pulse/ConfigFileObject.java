package com.zutubi.pulse.vfs.pulse;

import com.zutubi.prototype.config.ConfigurationPersistenceManager;
import com.zutubi.prototype.type.CollectionType;
import com.zutubi.prototype.type.ComplexType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.pulse.filesystem.FileSystemException;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.List;

/**
 * A file to represent the root of the server configuration.  Descendents of
 * this file represent paths into the config system.
 */
public class ConfigFileObject extends AbstractPulseFileObject
{
    private ConfigurationPersistenceManager configurationPersistenceManager;
    /**
     * This is the path into the configuration subsystem.
     */
    private String path;
    private ComplexType type;

    public ConfigFileObject(FileName name, AbstractFileSystem fs)
    {
        super(name, fs);
        path = "";
        type = null;
    }

    public ConfigFileObject(FileName name, AbstractFileSystem fs, String path, ComplexType type)
    {
        super(name, fs);
        this.path = path;
        this.type = type;
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        String childPath = PathUtils.getPath(path, fileName.getBaseName());
        Type childType = configurationPersistenceManager.getType(childPath);
        if(!(childType instanceof ComplexType))
        {
            throw new FileSystemException("Illegal path '" + childPath + "': does not refer to a valid type");
        }

        return objectFactory.buildBean(ConfigFileObject.class,
                new Class[]{FileName.class, AbstractFileSystem.class, String.class, ComplexType.class},
                new Object[]{fileName, pfs, childPath, (ComplexType) childType}
        );
    }

    protected FileType doGetType() throws Exception
    {
        if(type == null || configurationPersistenceManager.getListing(path).size() > 0 || configurationPersistenceManager.getType(path) instanceof CollectionType)
        {
            return FileType.FOLDER;
        }
        else
        {
            return FileType.FILE;
        }
    }

    protected String[] doListChildren() throws Exception
    {
        List<String> listing = configurationPersistenceManager.getListing(path);
        return listing.toArray(new String[listing.size()]);
    }

    public void setConfigurationPersistenceManager(ConfigurationPersistenceManager configurationPersistenceManager)
    {
        this.configurationPersistenceManager = configurationPersistenceManager;
    }
}
