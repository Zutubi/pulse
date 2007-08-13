package com.zutubi.pulse.core.scm;

import java.util.Properties;

/**
 * Used to transfer information about the SCM configuration of a project from
 * the Pulse server to the personal build client.  The client checks the
 * config appears to match the working copy it is looking at.
 */
public class ScmConfiguration
{
    public static final String TYPE_CVS = "cvs";
    public static final String TYPE_PERFORCE = "perforce";
    public static final String TYPE_SUBVERSION = "subversion";

    public static final String PROPERTY_TYPE = "type";
    
    /**
     * Indicates the type of SCM the project uses.  Represented as a string
     * to allow plugin implementations to define their own types easily.
     */
    private String type;
    /**
     * Properties that identify the repository used by the project
     * (SCM-specific).
     */
    private Properties repositoryDetails;

    public ScmConfiguration(String type)
    {
        this.type = type;
        this.repositoryDetails = new Properties();
    }

    public String getType()
    {
        return type;
    }

    public void addProperty(String key, String value)
    {
        repositoryDetails.setProperty(key, value);
    }

    public String getProperty(String key)
    {
        return (String) repositoryDetails.get(key);
    }

    public Properties getRepositoryDetails()
    {
        return repositoryDetails;
    }
}
