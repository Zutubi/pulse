package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.pulse.servercore.filesystem.FileSystemException;
import com.zutubi.tove.config.ConfigurationSecurityManager;
import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.type.ComplexType;
import com.zutubi.tove.type.Type;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.UriParser;

import java.util.Comparator;
import java.util.List;

/**
 * A file to represent the root of the server configuration.  Descendents of
 * this file represent paths into the config system.
 */
public class ConfigFileObject extends AbstractPulseFileObject implements ComparatorProvider
{
    private ConfigurationTemplateManager configurationTemplateManager;
    private ConfigurationSecurityManager configurationSecurityManager;
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
        String decodedBaseName = UriParser.decode(fileName.getBaseName());
        String childPath = PathUtils.getPath(path, decodedBaseName);
        Type childType = configurationTemplateManager.getType(childPath);
        if(!(childType instanceof ComplexType))
        {
            if (childType == null)
            {
                // Could be a type that is not registered (missing plugin?).
                return objectFactory.buildBean(ConfigErrorFileObject.class,
                        new Class[]{FileName.class, AbstractFileSystem.class, String.class, ComplexType.class, ComplexType.class, Record.class, String.class},
                        new Object[]{fileName, pfs, childPath, type, null, null, "Path does not have a type: perhaps it is from a missing plugin?"}
                );
            }

            throw new FileSystemException("Illegal path '" + childPath + "': does not refer to a valid type");
        }

        Record childValue;
        if(value == null)
        {
            childValue = configurationTemplateManager.getRecord(childPath);
        }
        else
        {
            childValue = (Record) value.get(decodedBaseName);
        }

        return objectFactory.buildBean(ConfigFileObject.class,
                new Class[]{FileName.class, AbstractFileSystem.class, String.class, ComplexType.class, ComplexType.class, Record.class},
                new Object[]{fileName, pfs, childPath, type, (ComplexType) childType, childValue}
        );
    }

    public String getDisplayName()
    {
        return ToveUtils.getDisplayName(path, type, parentType, value);
    }

    protected FileType doGetType() throws Exception
    {
        if(type == null || ToveUtils.isFolder(path, configurationTemplateManager, configurationSecurityManager))
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

    public String getIconCls()
    {
        return ToveUtils.getIconCls(type);
    }

    protected String[] doListChildren() throws Exception
    {
        List<String> listing = ToveUtils.getPathListing(path, type, configurationTemplateManager, configurationSecurityManager);
        return UriParser.encode(listing.toArray(new String[listing.size()]));
    }

    public Comparator<FileObject> getComparator()
    {
        // Indicates pre-sorted children.
        return null;
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }

    public void setConfigurationSecurityManager(ConfigurationSecurityManager configurationSecurityManager)
    {
        this.configurationSecurityManager = configurationSecurityManager;
    }
}
