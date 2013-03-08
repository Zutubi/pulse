package com.zutubi.pulse.master.vfs.provider.pulse;

import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.provider.AbstractFileSystem;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class WizardsFileObject extends AbstractPulseFileObject
{
    private static final Map<String, Class<? extends AbstractPulseFileObject>> nodesDefinitions = new HashMap<String, Class<? extends AbstractPulseFileObject>>();
    static
    {
        nodesDefinitions.put("projects", ProjectWizardsFileObject.class);
    }

    public WizardsFileObject(final FileName name, final AbstractFileSystem fs)
    {
        super(name, fs);
    }

    public AbstractPulseFileObject createFile(FileName fileName)
    {
        String name = fileName.getBaseName();
        if (nodesDefinitions.containsKey(name))
        {
            Class<? extends AbstractPulseFileObject> clazz = nodesDefinitions.get(name);
            return objectFactory.buildBean(clazz,
                    new Class[]{FileName.class, AbstractFileSystem.class},
                    new Object[]{fileName, pfs}
            );
        }
        return null;
    }

    protected FileType doGetType() throws Exception
    {
        return FileType.FOLDER;
    }

    protected String[] doListChildren() throws Exception
    {
        // do not support listing.
        return NO_CHILDREN;
    }

    public boolean isLocal()
    {
        return true;
    }
}
