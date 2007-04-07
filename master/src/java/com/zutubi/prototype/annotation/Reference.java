package com.zutubi.prototype.annotation;

import com.zutubi.prototype.OptionProvider;
import com.zutubi.pulse.form.FieldType;
import com.zutubi.pulse.prototype.config.DefaultReferenceOptionProvider;

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

@Field(type = FieldType.SELECT)
@Handler(ReferenceAnnotationHandler.class)
public @interface Reference
{
    Class<? extends OptionProvider> optionProvider() default DefaultReferenceOptionProvider.class;
}
