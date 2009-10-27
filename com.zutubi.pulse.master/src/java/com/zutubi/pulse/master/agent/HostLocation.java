package com.zutubi.pulse.master.agent;

/**
 * Abstracts the notion of a host location shared by hosts and agents.
 */
public interface HostLocation
{
    /**
     * Indicates if this is a remote host.
     *
     * @return true if the host is remote, false if it is the local machine
     */
    boolean isRemote();

    /**
     * Returns the name of the host for remote locations.
     *
     * @return the hostname of the remote location
     */
    String getHostName();

    /**
     * Returns the port for remote locations.
     *
     * @return the port number of the remote location
     */
    int getPort();
}
