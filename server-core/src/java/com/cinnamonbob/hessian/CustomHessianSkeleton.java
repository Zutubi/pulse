package com.cinnamonbob.hessian;

import com.caucho.hessian.server.HessianSkeleton;

/**
 */
public class CustomHessianSkeleton extends HessianSkeleton
{
    /**
     * Create a new hessian skeleton.
     *
     * @param service  the underlying service object.
     * @param apiClass the API interface
     */
    public CustomHessianSkeleton(Object service, Class apiClass)
    {
        super(service, apiClass);
    }
}
