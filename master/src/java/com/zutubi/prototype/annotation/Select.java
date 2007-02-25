package com.zutubi.prototype.annotation;

import com.zutubi.prototype.OptionProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)

@Field(type= "select")
@Handler(SelectAnnotationHandler.class)
public @interface Select
{
    Class<? extends OptionProvider> value();
}
