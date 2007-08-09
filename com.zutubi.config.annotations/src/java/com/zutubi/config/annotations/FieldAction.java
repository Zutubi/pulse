package com.zutubi.config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to add a custom action to a form field.  Actions are rendered as
 * links after the field.  When the link is clicked an event is raised on the
 * field with the action key.  A template can be associated with the link to
 * handle this event.
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Handler(className = DefaultAnnotationHandlers.FIELD_ACTION)
public @interface FieldAction
{
    /**
     * @return the name of the template used to render the link's associated
     * code.
     */
    public String template() default "";

    /**
     * @return the i18n key for the link.
     */
    public String actionKey() default "browse";
}
