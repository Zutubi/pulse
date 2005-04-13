package com.cinnamonbob.core;

import java.io.File;

import nu.xom.Element;

/**
 * Specification for a command artifact.  Describes both the artifact and how
 * to collect it (if necessary).
 * 
 * @author jsankey
 */
public class ArtifactSpec
{
    private static final String CONFIG_ATTR_NAME      = "name";
    private static final String CONFIG_ATTR_TITLE     = "title";
    private static final String CONFIG_ATTR_TYPE      = "type";
    private static final String CONFIG_ATTR_FROM_FILE = "from-file";
    private static final String CONFIG_ATTR_TO_FILE   = "to-file";
    
    
    private String name;
    private String title;
    private String type;
    private File fromFile;
    private File toFile;
    
    
    private void loadConfig(ConfigContext context, Element element) throws ConfigException
    {
        name  = XMLConfigUtils.getAttributeValue(context, element, CONFIG_ATTR_NAME);
        // FIXME
        title = XMLConfigUtils.getAttributeValue(context, element, CONFIG_ATTR_TITLE, name);
        type  = XMLConfigUtils.getAttributeValue(context, element, CONFIG_ATTR_TYPE);
        
        fromFile = new File(XMLConfigUtils.getAttributeValue(context, element, CONFIG_ATTR_FROM_FILE));
        
        String toFileName = XMLConfigUtils.getAttributeValue(context, element, CONFIG_ATTR_TO_FILE, fromFile.getName());
        toFile = new File(toFileName);
    }

    
    public ArtifactSpec(ConfigContext context, Element element) throws ConfigException
    {
        loadConfig(context, element);
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


    public String getName()
    {
        return name;
    }

    
    public String getTitle()
    {
        return title;
    }
    

    public File getFromFile()
    {
        return fromFile;
    }
    
    
    public File getToFile()
    {
        return toFile;
    }


    public String getType()
    {
        return type;
    }
    
    
    public boolean requiresCollection()
    {
        return fromFile != null;
    }
}
