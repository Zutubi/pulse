package com.zutubi.pulse.license;

/**
 * This interface gives us something to refer to in our code that is
 * not the actual license key generator. This will help to ensure that
 * code dependent on the license key generator will at least compile
 * without it being available. 
 */
public interface LicenseKeyFactory
{
    byte[] encode(License license) throws LicenseException;
}
