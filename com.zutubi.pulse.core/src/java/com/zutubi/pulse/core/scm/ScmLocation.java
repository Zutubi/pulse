package com.zutubi.pulse.core.scm;

/**
 * Used to transfer information about the SCM configuration of a project from
 * the Pulse server to the personal build client.  The client checks the
 * config appears to match the working copy it is looking at.
 */
public class ScmLocation
{
    public static final String TYPE     = "type";
    public static final String LOCATION = "location";
    
    /**
     * Indicates the type of SCM the project uses.  Represented as a string
     * to allow plugin implementations to define their own types easily.
     */
    private String type;
    /**
     * The location of the project in the SCM according to the master
     * configuration.
     */
    private String location;

    public ScmLocation(String type, String location)
    {
        this.type = type;
        this.location = location;
    }

    public String getType()
    {
        return type;
    }

    public String getLocation()
    {
        return location;
    }
}
