package com.cinnamonbob.core;

import java.util.Properties;

/**
 * Records context information used during loading of a config file.
 * 
 * @author jsankey
 */
public class ConfigContext
{
    /**
     * Name of the config file being loaded.
     */
    private String filename;
    /**
     * Mapping from variable names to values.
     */
    private Properties variables;
    
    //=======================================================================
    // Construction
    //=======================================================================

    /**
     * Create a new empty context for the given config file.
     * 
     * @param filename
     *        the name of the config file being loaded
     */
    public ConfigContext(String filename)
    {
        this.filename = filename;
        this.variables = new Properties();
    }
    
    //=======================================================================
    // Interface
    //=======================================================================

    /**
     * Returns the name of the config file being loaded.
     * 
     * @return the name of the file being loaded
     */
    public String getFilename()
    {
        return filename;
    }
    
    /**
     * Sets the variable of the given name to the given value, creating it if
     * it does not exist.
     * 
     * @param name
     *        the variable name
     * @param value
     *        the new variable value
     */
    public void setVariable(String name, String value)
    {
        variables.setProperty(name, value);
    }
    
    /**
     * Returns true iff the given variable exists.
     * 
     * @param name
     *        the variable name to test
     * @return true if this context contains a variable fo the given name
     */
    public boolean hasVariable(String name)
    {
        return variables.containsKey(name);
    }
    
    /**
     * Returns the current value of the given variable, or null if it does
     * not exist.
     * 
     * @param name
     *        the name of the variable to look up
     * @return the value of the given variable
     */
    public String getVariableValue(String name)
    {
        return variables.getProperty(name);
    }
}
