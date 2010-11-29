package com.zutubi.pulse.master.license;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.util.Collection;

/**
 * A method interceptor implementation that enforces licensing requirements..
 */
public class LicenseInterceptor implements MethodInterceptor
{
    private LicenseAnnotationAttributes annotationAttributes = new LicenseAnnotationAttributes();

    public Object invoke(MethodInvocation methodInvocation) throws Throwable
    {
        Collection<String> attributes = annotationAttributes.getAttributes(methodInvocation.getMethod());

        LicenseHolder.ensureAuthorization(attributes.toArray(new String[attributes.size()]));

        return methodInvocation.proceed();
    }
}
