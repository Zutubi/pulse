package com.cinnamonbob;

import com.cinnamonbob.core.ResourceRepository;
import com.cinnamonbob.core.model.Property;
import com.cinnamonbob.core.model.Resource;
import com.cinnamonbob.core.util.SystemUtils;
import com.cinnamonbob.model.persistence.ResourceDao;

import java.io.File;

/**
 */
public class ResourceDiscoverer implements Runnable
{
    private ResourceRepository resourceRepository;
    private ResourceDao resourceDao;

    public void run()
    {
        discoverAnt();
    }

    private void discoverAnt()
    {
        if (!resourceRepository.hasResource("ant"))
        {
            String home = System.getenv("ANT_HOME");
            if (home != null)
            {
                Resource antResource = new Resource("ant");
                antResource.addProperty(new Property("ant.home", "home"));
                File antBin;

                if (SystemUtils.isWindows())
                {
                    antBin = new File(home, "bin/ant.bat");
                }
                else
                {
                    antBin = new File(home, "bin/ant");
                }

                if (antBin.isFile())
                {
                    antResource.addProperty(new Property("ant.bin", antBin.getAbsolutePath()));
                }

                File antLib = new File(home, "lib");
                if (antLib.isDirectory())
                {
                    antResource.addProperty(new Property("ant.lib.dir", antLib.getAbsolutePath()));
                }

                resourceDao.save(antResource);
            }
        }
    }

    public void setResourceRepository(ResourceRepository resourceRepository)
    {
        this.resourceRepository = resourceRepository;
    }

    public void setResourceDao(ResourceDao resourceDao)
    {
        this.resourceDao = resourceDao;
    }
}
