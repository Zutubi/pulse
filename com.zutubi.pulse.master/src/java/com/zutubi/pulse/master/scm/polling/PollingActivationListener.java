package com.zutubi.pulse.master.scm.polling;

/**
 * A callback interface that provides notifications of a requests activation.
 */
public interface PollingActivationListener
{
    /**
     * Called when a request is about to be activated.
     *
     * @param request   the request to be activated.
     */
    void onActivation(PollingRequest request);
}
