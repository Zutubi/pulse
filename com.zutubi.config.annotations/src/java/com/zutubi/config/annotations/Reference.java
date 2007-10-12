package com.zutubi.config.annotations;

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

@Field(type = FieldType.ITEM_PICKER)
@Handler(className = DefaultAnnotationHandlers.REFERENCE)
public @interface Reference
{
    String optionProvider() default "com.zutubi.pulse.prototype.config.DefaultReferenceOptionProvider";
    String cleanupTaskProvider() default "com.zutubi.prototype.config.cleanup.DefaultReferenceCleanupTaskProvider";
}
