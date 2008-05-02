package com.zutubi.pulse.web.admin;

import com.opensymphony.xwork.Validateable;
import com.zutubi.pulse.SlaveProxyFactory;
import com.zutubi.pulse.core.model.Resource;
import com.zutubi.pulse.model.ResourceManager;
import com.zutubi.pulse.model.Slave;
import com.zutubi.pulse.model.SlaveManager;
import com.zutubi.pulse.resources.*;
import com.zutubi.pulse.services.SlaveService;
import com.zutubi.pulse.web.wizard.BaseWizard;
import com.zutubi.pulse.web.wizard.BaseWizardState;
import com.zutubi.pulse.web.wizard.Wizard;

import java.util.Map;
import java.util.TreeMap;

/**
 * <class comment/>
 */
public class AddResourceWizard extends BaseWizard
{
    private long agentId = -1;

    private Select select;

    private ResourceManager resourceManager;

    private SlaveManager slaveManager;

    private SlaveProxyFactory slaveProxyFactory;

    private Custom custom;

    public AddResourceWizard()
    {
        select = new Select(this, "select");
        addInitialState(select);

        addState(new Directory(this, "ant", new AntResourceConstructor()));
        addState(new Directory(this, "java", new JavaResourceConstructor()));
        addState(new Directory(this, "maven", new MavenResourceConstructor()));
        addState(new Directory(this, "maven2", new Maven2ResourceConstructor()));
        
        custom = new Custom(this, "custom");
        addState(custom);
    }

    public void initialise()
    {
        super.initialise();

        // if agent is master, then we can browse the filesystem. Otherwise, we can not... yet.
    }

    public void process()
    {
        super.process();

        Resource resource;
        if (select.getType().equals("custom"))
        {
            // create a new resource using the new name.
            resource = new Resource(custom.getName());
        }
        else
        {
            Directory d = (Directory) getState(select.getType());
            resource = createResource(d.getConstructor(), d.getDir());
        }
        
        if (resource != null)
        {
            resourceManager.addResource(getSlave(), resource);
        }
    }

    public long getAgentId()
    {
        return agentId;
    }

    public void setAgentId(long agentId)
    {
        this.agentId = agentId;
    }

    public Slave getSlave()
    {
        return slaveManager.getSlave(agentId);
    }

    protected boolean isResourceHome(ResourceConstructor c, String path)
    {
        if (agentId != -1)
        {
            return slaveProxyFactory.createProxy(getSlave()).isResourceHome(c, path);
        }
        else
        {
            return c.isResourceHome(path);
        }
    }

    protected Resource createResource(ResourceConstructor c, String path)
    {
        try
        {
            if (agentId != -1)
            {
                Slave slave = slaveManager.getSlave(agentId);
                SlaveService proxy = slaveProxyFactory.createProxy(slave);
                return proxy.createResource(c, path);
            }
            else
            {
                return c.createResource(path);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public void setResourceManager(ResourceManager resourceManager)
    {
        this.resourceManager = resourceManager;
    }

    public void setSlaveManager(SlaveManager slaveManager)
    {
        this.slaveManager = slaveManager;
    }

    public void setSlaveProxyFactory(SlaveProxyFactory slaveProxyFactory)
    {
        this.slaveProxyFactory = slaveProxyFactory;
    }

    private class Select extends BaseWizardState
    {
        private Map<String, String> types;

        private String type;

        public Select(Wizard wizard, String name)
        {
            super(wizard, name);
        }

        public String getNextStateName()
        {
            return getType();
        }

        public String getType()
        {
            return type;
        }

        public void setType(String type)
        {
            this.type = type;
        }

        public Map<String, String> getTypes()
        {
            if (types == null)
            {
                types = new TreeMap<String, String>();
                types.put("ant", "ant");
                types.put("custom", "custom");
                types.put("java", "java");
                types.put("maven", "maven");
                types.put("maven2", "maven2");
            }
            return types;
        }
    }

    private class Directory extends BaseWizardState implements Validateable
    {
        private String dir;

        private ResourceConstructor constructor;

        public Directory(Wizard wizard, String name, ResourceConstructor constructor)
        {
            super(wizard, name);
            this.constructor = constructor;
        }

        public String getNextStateName()
        {
            return "success";
        }

        public void setDir(String dir)
        {
            this.dir = dir;
        }

        public String getDir()
        {
            return dir;
        }

        public void validate()
        {
            if (hasErrors())
            {
                return;
            }
            if (dir != null)
            {
                dir = dir.trim();
            }
            if (!isResourceHome(constructor, dir))
            {
                addFieldError("dir", "The selected directory is not recognised as a base directory for this resource type.");
            }
        }

        public ResourceConstructor getConstructor()
        {
            return constructor;
        }
    }

    private class Custom extends BaseWizardState implements Validateable
    {
        private String name;

        public Custom(Wizard wizard, String name)
        {
            super(wizard, name);
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public String getNextStateName()
        {
            return "success";
        }

        public void validate()
        {
            if (hasErrors())
            {
                return;
            }
            
            Slave slave = getSlave();

            if (resourceManager.findBySlaveAndName(slave, name) != null)
            {
                addFieldError("name", String.format("A resource with name '%s' already exists.", name));
            }
        }
    }
}
