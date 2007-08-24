package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.model.Revision;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class ScmContext
{
    private String id;

    private Revision revision;

    private File dir;

    private List<Property> props = new LinkedList<Property>();

    public Revision getRevision()
    {
        return revision;
    }

    public void setRevision(Revision revision)
    {
        this.revision = revision;
    }

    public File getDir()
    {
        return dir;
    }

    public void setDir(File dir)
    {
        this.dir = dir;
    }

    /**
     * Specify variables that should be added to the build environment for
     * this SCM.  This is useful if commands run in the build may need access
     * to the information, but may be unnecessary for SCMs where the
     * information is already present in the working copy (e.g. Subversion).
     * In this latter case an empty map may be returned.
     *
     * Required for all implementations.
     *
     * @param key name of the variable
     * @param value of the variable
     * @param addToEnv indicates whether or not the variable should be added to the build environment.
     */
    public void addProperty(String key, String value, boolean addToEnv)
    {
        props.add(new Property(key, value, addToEnv));
    }

    public void addProperty(String key, String value)
    {
        props.add(new Property(key, value));
    }

    public List<Property> getProperties()
    {
        return props;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public static class Property
    {
        private String name;
        private String value;
        private boolean addToEnvironment = false;
        private boolean addToPath = false;
        private boolean resolveVariables = false;

        public Property(String name, String value)
        {
            this.value = value;
            this.name = name;
        }

        public Property(String name, String value, boolean addToEnvironment)
        {
            this.name = name;
            this.value = value;
            this.addToEnvironment = addToEnvironment;
        }

        public Property(String name, String value, boolean addToEnvironment, boolean addToPath, boolean resolveVariables)
        {
            this.name = name;
            this.value = value;
            this.addToEnvironment = addToEnvironment;
            this.addToPath = addToPath;
            this.resolveVariables = resolveVariables;
        }

        public String getValue()
        {
            return value;
        }

        public String getName()
        {
            return name;
        }

        public boolean isAddToEnvironment()
        {
            return addToEnvironment;
        }

        public boolean isAddToPath()
        {
            return addToPath;
        }

        public boolean isResolveVariables()
        {
            return resolveVariables;
        }
    }
}
