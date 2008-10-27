package com.zutubi.tove.squeezer;

/**
 * <class-comment/>
 */
public interface TypeSqueezer
{
    String squeeze(Object obj) throws SqueezeException;

    Object unsqueeze(String... str) throws SqueezeException;

}
