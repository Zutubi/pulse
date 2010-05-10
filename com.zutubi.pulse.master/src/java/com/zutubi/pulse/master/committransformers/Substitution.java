package com.zutubi.pulse.master.committransformers;

/**
 * Represents a single subtitution to be made by a commit message transformer.
 */
public interface Substitution
{
    /**
     * Returns the regular expression used to match the text to transform.
     * 
     * @return a regular expression that will match the text to transform
     */
    String getExpression();

    /**
     * Returns the text used to replaced matched input.
     * 
     * @return a string used to replace matched input, which will usually
     *         contain references to to the input and/or groups from the
     *         expression
     */
    String getReplacement();
}
