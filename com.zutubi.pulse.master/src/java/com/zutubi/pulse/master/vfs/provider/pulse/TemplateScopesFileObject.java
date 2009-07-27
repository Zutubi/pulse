package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.tove.config.ConfigurationTemplateManager;
import com.zutubi.util.Sort;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;
import org.apache.commons.vfs.provider.UriParser;

import java.util.Arrays;
import java.util.Set;

/**
 * Root of a file system that models template hierarchies.  The direct
 * children of this node are the scopes of the hierarchies.  The next level
 * is the root template, then its children and so on, e.g.:
 * <p/>
 * pulse://templates/project/global project template/my project/child project
 */
public class TemplateScopesFileObject extends AbstractPulseFileObject
{
    private ConfigurationTemplateManager configurationTemplateManager;

    public TemplateScopesFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(FileName fileName)
    {
        return objectFactory.buildBean(TemplateScopeFileObject.class,
                                       new Class[]{FileName.class, AbstractFileSystem.class},
                                       new Object[]{fileName, pfs}
        );
    }

    protected FileType doGetType() throws Exception
    {
        // allow traversal of this node.
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        Set<String> scopes = configurationTemplateManager.getTemplateScopes();
        String[] children = new String[scopes.size()];
        scopes.toArray(children);
        Arrays.sort(children, new Sort.StringComparator());
        return UriParser.encode(children);
    }

    public void setConfigurationTemplateManager(ConfigurationTemplateManager configurationTemplateManager)
    {
        this.configurationTemplateManager = configurationTemplateManager;
    }
}
