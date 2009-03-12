package com.zutubi.pulse.servercore.services;

/**
 */
public interface TokenManager
{
    /**
     * Get the current token.
     *
     * @return current token.
     */
    String getToken();

    /**
     * Register a listener that will be notified whenever the token
     * is updated.
     *
     * @param listener to receive the notifications.
     */
    void register(TokenManagerListener listener);

    /**
     * Unregister a registered listener so that it will no longer receive
     * notifications when the token is updated.
     *
     * @param listener to no longer receive notifications.
     */
    void unregister(TokenManagerListener listener);
}
