package com.zutubi.pulse.master.model.persistence.hibernate;

/**
 */
public abstract class MasterPersistenceTestCase extends PersistenceTestCase
{
    public MasterPersistenceTestCase()
    {
        super();
    }

    public MasterPersistenceTestCase(String testName)
    {
        super(testName);
    }

    protected String[] getConfigLocations()
    {
        return new String[]{
                "com/zutubi/pulse/master/bootstrap/testBootstrapContext.xml",
                "com/zutubi/pulse/master/bootstrap/context/hibernateContext.xml"
        };
    }
}
