package com.zutubi.pulse.core.engine.api;

/**
 * Used to mark a collection field as addable when binding text to
 * configuration objects.  For example, if your configuration type has a
 * property widgets of type Map&lt;String, Widget&gt;, you can annotate
 * that property as @Addable("widget").  Then when binding from an XML
 * form, a nested &lt;widget&gt; tag is understood to define a widget
 * instance which should be added to the widgets collection.
 */
public @interface Addable
{
    /**
     * Defines the names used to represent instances that can be added
     * to this collection property  (e.g. XML tag names).
     * 
     * @return acceptable names for defining instances to be added
     */
    String[] value();
}
