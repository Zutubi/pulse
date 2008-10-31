package com.zutubi.pulse.master.vfs.provider.pulse;

import com.zutubi.util.bean.ObjectFactory;
import org.apache.commons.vfs.*;
import org.apache.commons.vfs.provider.AbstractOriginatingFileProvider;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * <class comment/>
 */
public class PulseFileProvider extends AbstractOriginatingFileProvider
{
    private ObjectFactory objectFactory;

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
        try
        {
            return objectFactory.buildBean(PulseFileSystem.class,
                    new Class[]{FileName.class, FileObject.class, FileSystemOptions.class}, 
                    new Object[]{rootName, null, fileSystemOptions}
            );
        }
        catch (Exception e)
        {
            throw new FileSystemException(e);
        }
    }

    public Collection getCapabilities()
    {
        return CAPABILITIES;
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