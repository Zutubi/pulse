package com.zutubi.pulse.core.scm.cvs.validation.annotation;

import com.zutubi.validation.annotations.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation annotation for checking CVS root syntax.
 */
@Constraint("com.zutubi.pulse.core.scm.cvs.validation.validators.CvsRootValidator")
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CvsRoot
{
}
