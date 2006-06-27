package com.zutubi.pulse.model.persistence.hibernate;

/**
 */
public abstract class ServerCorePersistenceTestCase extends PersistenceTestCase
{
    protected String[] getConfigLocations()
    {
        return new String[]{
                "com/zutubi/pulse/bootstrap/testBootstrapContext.xml",
                "com/zutubi/pulse/model/persistence/hibernate/testHibernateContext.xml"
        };
    }
}
