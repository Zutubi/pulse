package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.model.Revision;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Context in which an SCM checkout or update operation is executed.
 * Contains both parameters to affect the operation (e.g. the revision) and
 * the ability to communicate information to the build (see {@link
 * #addProperty}).
 */
public class ScmContext
{
    private String id;
    private Revision revision;
    private File dir;
    private List<Property> props = new LinkedList<Property>();

    /**
     * @return an identifier used to link related operations.  Operations
     * dealing with the same persistent working copy will have the same id.
     * If the working copy is transient the id will be null.
     */
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    /**
     * @return he revision at which the operation should be performed.  May
     * be ignored by implementations that do not support {@link
     * ScmCapability#CHANGESETS}.
     */
    public Revision getRevision()
    {
        return revision;
    }

    public void setRevision(Revision revision)
    {
        this.revision = revision;
    }

    /**
     * @return location to use for the working copy.
     */
    public File getDir()
    {
        return dir;
    }

    public void setDir(File dir)
    {
        this.dir = dir;
    }

    /**
     * Add a property which will be propageted to the build environment.
     * This is useful if commands run in the build may need access to the
     * information, but may be unnecessary for SCMs where the information is
     * already present in the working copy (e.g. Subversion).
     *
     * @param key   name of the variable
     * @param value of the variable
     * @param addToEnv indicates whether or not the variable should be added
     *                 to the build environment along with the build context
     */
    public void addProperty(String key, String value, boolean addToEnv)
    {
        props.add(new Property(key, value, addToEnv));
    }

    /**
     * Identical to calling addProperty(key, value, false).
     *
     * @see #addProperty(String, String, boolean)
     *
     * @param key   name of the variable
     * @param value of the variable
     */
    public void addProperty(String key, String value)
    {
        props.add(new Property(key, value));
    }

    /**
     * @return additional, implementation-specific properties added during
     * the operation.  Properties added by the operation will be propagated
     * into the build context when the operation is complete.
     */
    public List<Property> getProperties()
    {
        return props;
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
