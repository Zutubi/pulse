package com.zutubi.pulse.servercore.validation.annotation;

import com.zutubi.validation.annotations.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validation annotation for checking custom subscription condition syntax.
 */
@Constraint("com.zutubi.pulse.validation.validators.SubscriptionConditionValidator")
@Target({ ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SubscriptionCondition
{
}
