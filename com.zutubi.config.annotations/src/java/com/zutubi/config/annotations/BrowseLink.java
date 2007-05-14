package com.zutubi.config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Handler(className = DefaultAnnotationHandlers.BROWSE_LINK)
public @interface BrowseLink
{
    /**
     * @return the name of the template used to render the link's associated
     * code.
     */
    public String template();

    /**
     * @return the i18n key for the link.
     */
    public String linkKey() default "browse";
}
