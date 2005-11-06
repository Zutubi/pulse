package com.cinnamonbob;

/**
 * This interface allows the implementation of multiple dispatch strategies.
 * For example, one possible implementation would be to support a round robin
 * style strategy for dispatching to multiple destinations.
 * 
 */
public interface BuildDispatcher
{
    /**
     * Receive a request to dispatch the build request to the
     * appropriate destination for processing.
     *
     * @param request
     */
    void dispatch(BuildRequest request);
}
