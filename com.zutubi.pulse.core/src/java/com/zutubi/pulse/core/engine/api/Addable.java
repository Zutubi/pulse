package com.zutubi.pulse.core.engine.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to mark a collection field as addable when binding text to
 * configuration objects.  For example, if your configuration type has a
 * property widgets of type Map&lt;String, Widget&gt;, you can annotate
 * that property as @Addable("widget").  Then when binding from an XML
 * form, a nested &lt;widget&gt; tag is understood to define a widget
 * instance which should be added to the widgets collection.
 * <p/>
 * If the collection field contains simple values (including references),
 * then {@link #attribute()} gives the name of the attribute that will
 * contain the value or referencing text.  For example, with reference()
 * set to "ref" (the default), a widget tag would refer to a widget like
 * &lt;widget ref="${widget.property}"/&gt;.  If attribute() is empty, then
 * the nested text between the tags is used.
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Addable
{
    /**
     * Defines the name used to represent instances that can be added
     * to this collection property  (e.g. the XML tag name).
     * 
     * @return the name for defining an instance to be added
     */
    String value();

    /**
     * Defines the attribute used to refer to a widget when the target
     * collection holds references rather than values.  May be empty to
     * indicate that nested text content should be used.
     *
     * @return the name of the referencing attribute
     */
    String attribute() default "ref";
}
