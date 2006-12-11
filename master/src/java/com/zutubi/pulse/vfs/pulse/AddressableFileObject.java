package com.zutubi.pulse.vfs.pulse;

import org.apache.commons.vfs.FileSystemException;

/**
 * An addressable file object is one that has a physical URL representation.
 *
 * For example:
 *
 * <ul>
 * <li>/projects -> is represented by the projects home page.</li>
 * <li>/projects/<projectName>/builds/<buildNumber> represents the build page for build x in project y.</li>
 * </ul>
 * 
 */
public interface AddressableFileObject
{
    /**
     * Get the URL path representation for this file object. This path should represent the path from
     * the application context. That is, it should not include the url scheme, nor should it attempt to
     * prefix the web app context.
     *
     * @return url path string
     *
     * @throws FileSystemException if there is a problem constructing the url path. This will typically occur when
     * the addressable file object represents a non-existance resource.
     */
    String getUrlPath() throws FileSystemException;
}
