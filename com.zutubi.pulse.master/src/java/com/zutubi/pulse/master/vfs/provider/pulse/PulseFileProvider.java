package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.util.bean.ObjectFactory;
import org.apache.commons.vfs.*;
import org.apache.commons.vfs.provider.AbstractOriginatingFileProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * The PulseFileProvider is the FileProvider implementation that integrates the
 * PulseFileSystem into the VFS.
 */
public class PulseFileProvider extends AbstractOriginatingFileProvider
{
    private ObjectFactory objectFactory;

    private Class<? extends FileObject> rootFileType = RootFileObject.class;

    final static Collection CAPABILITIES = Collections.unmodifiableCollection(Arrays.asList(
        Capability.GET_TYPE,
        Capability.READ_CONTENT,
        Capability.URI,
        Capability.GET_LAST_MODIFIED
    ));

    public PulseFileProvider()
    {
        setFileNameParser(PulseFileNameParser.getInstance());
    }

    protected FileSystem doCreateFileSystem(final FileName rootName, final FileSystemOptions fileSystemOptions) throws FileSystemException
    {
        return objectFactory.buildBean(PulseFileSystem.class, rootName, null, fileSystemOptions, rootFileType);
    }

    public Collection getCapabilities()
    {
        return CAPABILITIES;
    }

    /**
     * Set the root file type to be used by the generated PulseFileSystem.  This root file type
     * defines the file structure that will be available through the PulseFileSystem.
     *
     * @param rootFileType the root file type.
     *
     * @see com.zutubi.pulse.master.vfs.provider.pulse.RootFileObject
     */
    public void setRootFileType(Class<? extends FileObject> rootFileType)
    {
        this.rootFileType = rootFileType;
    }

    /**
     * Required resource.
     *
     * @param objectFactory instance.
     */
    public void setObjectFactory(ObjectFactory objectFactory)
    {
        this.objectFactory = objectFactory;
    }
}