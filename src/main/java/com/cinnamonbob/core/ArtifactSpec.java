package com.cinnamonbob.core;

import java.io.File;

/**
 * Specification for a command artifact.  Describes both the artifact and how
 * to collect it (if necessary).
 * 
 * @author jsankey
 */
public class ArtifactSpec
{
    /**
     * The name of the artifact, unique in the command.
     */
    private String name;
    /**
     * The artifact title, used when displaying information about the artifact.
     */
    private String title;
    /**
     * The type of artifact, which loosely determines what can be done with
     * it (for example which post-processors may be applied to it).
     */
    private String type;
    /**
     * The source file for the artifact, or null if it is not collected from
     * elsewhere (i.e. it is already in the build directory).
     */
    private File fromFile;
    /**
     * The destination file to copy the artifact to.  Relative to the build
     * directory.
     */
    private File toFile;
    
    //=======================================================================
    // Construction
    //=======================================================================

    public ArtifactSpec() 
    {
    }
    
    /**
     * Constructs an artifact spec for an artifact that will be created
     * automatically as the result of executing a command.  These artifacts
     * need not be collected.
     * 
     * @param name
     *        the name of the artifact
     * @param title
     *        the title of the artifact, or null to default to the name
     * @param type
     *        the type of the artifact
     * @param toFile
     *        the output file created by the artifact (should be a relative
     *        path)
     */
    public ArtifactSpec(String name, String title, String type, File toFile)
    {
        this.name = name;
        if(title == null)
        {
            this.title = name;
        }
        else 
        {
            this.title = title;
        }
        
        this.type = type;
        fromFile = null;
        this.toFile = toFile;
    }

    //=======================================================================
    // Interface
    //=======================================================================

    /**
     * @return the artifact name
     */
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    
    /**
     * @return the artifact title
     */
    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }
    
    /**
     * @return the source file for the artifact
     */
    public File getFromFile()
    {
        return fromFile;
    }
    
    public void setFromFile(File f)
    {
        this.fromFile = f;
    }
    
    /**
     * @return the destination file for the artifact
     */
    public File getToFile()
    {
        return toFile;
    }

    public void setToFile(File f)
    {
        this.toFile = f;
    }
    
    /**
     * @return the artifact type
     */
    public String getType()
    {
        return type;
    }
    
    public void setType(String type)
    {
        this.type = type;
    }
    
    /**
     * Indicates if the artifact needs to be copied from source to destination
     * file.
     * 
     * @return true iff the artifact needs to be copied (collected) from a
     *         source file
     */
    public boolean requiresCollection()
    {
        return fromFile != null;
    }
}
