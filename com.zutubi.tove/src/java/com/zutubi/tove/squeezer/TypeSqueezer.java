package com.zutubi.tove.squeezer;

/**
 * Interface for squeezers: classes that can convert between objects and
 * strings.
 */
public interface TypeSqueezer
{
    /**
     * Converts the given object to a string representation.
     *
     * @param obj the object to convert
     * @return a string representation of the object
     * @throws SqueezeException if the object cannot be converted
     */
    String squeeze(Object obj) throws SqueezeException;

    /**
     * Converts the given string representation to the corresponding object.
     *
     * @param str the string to convert
     * @return the object represented by the string
     * @throws SqueezeException if the string cannot be converted
     */
    Object unsqueeze(String str) throws SqueezeException;
}
