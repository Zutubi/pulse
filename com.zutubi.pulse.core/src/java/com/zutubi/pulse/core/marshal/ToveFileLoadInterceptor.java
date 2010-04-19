package com.zutubi.pulse.core.marshal;

import com.zutubi.pulse.core.engine.api.Scope;
import com.zutubi.tove.config.api.Configuration;
import nu.xom.Element;

/**
 * Callback interface used to control the type loading process.
 * 
 * @see ToveFileLoader
 */
public interface ToveFileLoadInterceptor
{
    /**
     * Called when an instance has just been encountered, but its contents
     * (nested elements) have not yet been loaded.  Attributes are bound to
     * simple properties before this method is called.  This method can control
     * whether nested elements are processed or not.
     * 
     * @param instance the instance that has just been encountered, with
     *                 simple attributes bound 
     * @param element  the element that defines the instance
     * @param scope    the scope at the point of loading the element
     * @return true to load nested elements, false to skip them
     */
    boolean loadInstance(Configuration instance, Element element, Scope scope);

    /**
     * Indicates if unresolved properties should be allowed during loading of
     * an instance.
     * 
     * @param instance the instance being loaded
     * @param element  the element that defines the instance
     * @return true to allow unresolved properties, false to raise an error if
     *         an unresolved property is encountered
     */
    boolean allowUnresolved(Configuration instance, Element element);

    /**
     * Indicates if a loaded instance should be validated.  The instance is
     * fully loaded at the time of this call.
     * 
     * @param instance the instance being loaded
     * @param element  the element that defines the instance
     * @return true to validate the instance, false otherwise
     */
    boolean validate(Configuration instance, Element element);
}
