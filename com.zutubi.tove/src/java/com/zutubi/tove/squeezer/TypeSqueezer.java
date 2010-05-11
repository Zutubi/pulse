package com.zutubi.tove.squeezer;

/**
 * Interface for squeezers: classes that can convert between objects and
 * strings.
 */
public interface TypeSqueezer
{
    String squeeze(Object obj) throws SqueezeException;

    Object unsqueeze(String... str) throws SqueezeException;
}
