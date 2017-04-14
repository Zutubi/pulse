/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
