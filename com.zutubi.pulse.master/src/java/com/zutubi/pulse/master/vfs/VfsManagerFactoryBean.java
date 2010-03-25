package com.zutubi.pulse.master.vfs;

import com.zutubi.pulse.master.vfs.provider.local.DefaultLocalFileProvider;
import com.zutubi.pulse.master.vfs.provider.pulse.PulseFileProvider;
import com.zutubi.util.bean.ObjectFactory;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.cache.NullFilesCache;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.springframework.beans.factory.FactoryBean;

public class VfsManagerFactoryBean implements FactoryBean
{
    public static final String FS_LOCAL = "local";
    public static final String FS_PULSE = "pulse";

    private ObjectFactory objectFactory;

    private DefaultFileSystemManager instance;

    public Object getObject() throws Exception
    {
        synchronized(this)
        {
            if (instance == null)
            {
                instance = new DefaultFileSystemManager();
                instance.setFilesCache(new NullFilesCache());
                instance.addProvider(FS_LOCAL, new DefaultLocalFileProvider());

                PulseFileProvider pulseFileProvider = objectFactory.buildBean(PulseFileProvider.class);
                instance.addProvider(FS_PULSE, pulseFileProvider);

                instance.init();
            }
        }
        return instance;
    }

    public Class getObjectType()
    {
        return FileSystemManager.class;
    }

    public boolean isSingleton()
    {
        return true;
    }

    public void shutdown()
    {
        if (instance != null)
        {
            instance.close();
        }
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
