package com.zutubi.pulse.vfs.pulse;

import com.zutubi.prototype.config.ConfigurationTemplateManager;
import com.zutubi.prototype.type.ComplexType;
import com.zutubi.prototype.type.Type;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.webwork.PrototypeUtils;
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
    private ConfigurationTemplateManager configurationTemplateManager;
    /**
     * This is the path into the configuration subsystem.
     */
    private String path;
    private ComplexType parentType;
    private ComplexType type;
    private Record value;

    public ConfigFileObject(FileName name, AbstractFileSystem fs)
    {
        super(name, fs);
        path = "";
        parentType = null;
        type = null;
        value = null;
    }

    public ConfigFileObject(FileName name, AbstractFileSystem fs, String path, ComplexType parentType, ComplexType type, Record value)
    {
        super(name, fs);
        this.path = path;
        this.parentType = parentType;
        this.type = type;
        this.value = value;
    }

    public AbstractPulseFileObject createFile(final FileName fileName) throws Exception
    {
        String childPath = PathUtils.getPath(path, fileName.getBaseName());
        Type childType = configurationTemplateManager.getType(childPath);
        if(!(childType instanceof ComplexType))
        {
            throw new FileSystemException("Illegal path '" + childPath + "': does not refer to a valid type");
        }

        Record childValue;
        if(value == null)
        {
            childValue = configurationTemplateManager.getRecord(childPath);
        }
        else
        {
            childValue = (Record) value.get(fileName.getBaseName());
        }

        return objectFactory.buildBean(ConfigFileObject.class,
                new Class[]{FileName.class, AbstractFileSystem.class, String.class, ComplexType.class, ComplexType.class, Record.class},
                new Object[]{fileName, pfs, childPath, type, (ComplexType) childType, childValue}
        );
    }

    public String getDisplayName()
    {
        return PrototypeUtils.getDisplayName(path, parentType, value, configurationTemplateManager);
    }

    protected FileType doGetType() throws Exception
    {
        if(type == null || PrototypeUtils.isFolder(path, configurationTemplateManager))
        {
            return FileType.FOLDER;
        }
        else
        {
            return FileType.FILE;
        }
    }

    public String getCls()
    {
        if (configurationTemplateManager.pathExists(path))
        {
            return configurationTemplateManager.isDeeplyValid(path) ? null : "config-invalid";
        }
        else
        {
            return null;
        }
    }

    protected String[] doListChildren() throws Exception
    {
        List<String> listing = PrototypeUtils.getPathListing(path, type, configurationTemplateManager);
        return listing.toArray(new String[listing.size()]);
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }
}
