package com.zutubi.pulse.license;

/**
 * The license provider interface provides allows the location / source of the license to be decoupled
 * from its use.
 *
 * 
 */
public interface LicenseProvider
{
    /**
     * Retrieve an instance of the license.
     */
    License getLicense();
}
