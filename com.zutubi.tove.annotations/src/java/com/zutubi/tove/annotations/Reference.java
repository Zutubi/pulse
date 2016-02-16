package com.zutubi.tove.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to annotate properties to indicate that they are references to
 * records, rather than actual nested record values.
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)

@Field(type = FieldType.DROPDOWN)
@Handler(className = DefaultAnnotationHandlers.REFERENCE)
public @interface Reference
{
    String DEFAULT_dependentOn = "";

    String optionProvider() default "com.zutubi.tove.ui.handler.DefaultReferenceOptionProvider";
    String cleanupTaskProvider() default "com.zutubi.tove.config.cleanup.DefaultReferenceCleanupTaskProvider";

    /**
     * A reference field that is dependent on another field uses that
     * other field as the context instance for the option provider to
     * calculate the options.
     *
     * @return the field that this field depends on.
     */
    String dependentOn() default DEFAULT_dependentOn;
}
