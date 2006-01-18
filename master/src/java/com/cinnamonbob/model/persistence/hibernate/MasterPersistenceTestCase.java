package com.cinnamonbob.model.persistence.hibernate;

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
                "com/cinnamonbob/bootstrap/testBootstrapContext.xml",
                "com/cinnamonbob/bootstrap/testApplicationContext.xml",
                "com/cinnamonbob/bootstrap/databaseContext.xml"
        };
    }
}
