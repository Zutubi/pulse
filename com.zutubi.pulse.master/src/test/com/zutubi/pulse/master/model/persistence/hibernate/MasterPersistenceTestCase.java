package com.zutubi.pulse.master.model.persistence.hibernate;

public abstract class MasterPersistenceTestCase extends PersistenceTestCase
{
    protected String[] getConfigLocations()
    {
        return new String[]{
                "com/zutubi/pulse/master/bootstrap/testBootstrapContext.xml",
                "com/zutubi/pulse/master/bootstrap/context/hibernateContext.xml"
        };
    }
}
