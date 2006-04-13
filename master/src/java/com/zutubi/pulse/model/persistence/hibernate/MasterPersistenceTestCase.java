/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.model.persistence.hibernate;

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
                "com/zutubi/pulse/bootstrap/testBootstrapContext.xml",
                "com/zutubi/pulse/bootstrap/context/hibernateContext.xml"
        };
    }
}
