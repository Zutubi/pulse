package com.zutubi.pulse.prototype.squeezer;

/**
 * <class-comment/>
 */
public interface TypeSqueezer
{
    String squeeze(Object obj) throws SqueezeException;

    Object unsqueeze(String... str) throws SqueezeException;

}
