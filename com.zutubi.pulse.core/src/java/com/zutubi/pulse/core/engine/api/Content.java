package com.zutubi.pulse.core.engine.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to mark a text property as settable using the content contained within
 * XML tags.  When binding XML to objects, if text content is found between
 * tags it will be used to set a property with this annotation (if any).
 *
 * <p/>
 *
 * For example, given this configuration class:
 * <pre>{@code
 * public class MyConfig extends AbstractConfiguration
 * {
 *     \@Content
 *     private String text;
 * }
 * }</pre>
 *
 * The given XML would set the "text" property to "value":
 * 
 * <pre>{@code
 * <myconfig>value</myconfig>
 * }</pre>
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Content
{
}