package com.zutubi.pulse.tove.config.project.hooks;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CompatibleHooks
{
    Class<? extends BuildHookConfiguration>[] value();
}
