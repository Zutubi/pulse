package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.tove.config.TemplateHierarchy;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.UriParser;

/**
 * Represents a template hierarchy.  Nested directly underneath is the file
 * for the root template.
 *
 * @see com.zutubi.pulse.master.vfs.provider.pulse.TemplateScopesFileObject
 */
public class TemplateScopeFileObject extends AbstractPulseFileObject
{
    private ConfigurationTemplateManager configurationTemplateManager;

    public TemplateScopeFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(final FileName fileName)
    {
        return objectFactory.buildBean(TemplateFileObject.class, fileName, pfs);
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        TemplateHierarchy hierarchy = getTemplateHierarchy();
        if(hierarchy != null && hierarchy.getRoot() != null)
        {
            return new String[] { UriParser.encode(hierarchy.getRoot().getId()) };
        }

        return NO_CHILDREN;
    }

    public TemplateHierarchy getTemplateHierarchy() throws FileSystemException
    {
        return configurationTemplateManager.getTemplateHierarchy(UriParser.decode(getName().getBaseName()));
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }
}
