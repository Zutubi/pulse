package com.zutubi.tove.config.api;

/**
 * Interface to be implemented by classes used to classify configuration
 * instances.  A classifier by convention has the same name as the
 * configuration class it works on, with a "Classifier" suffix added.  In many
 * cases the {@link com.zutubi.tove.annotations.Classification} annotation may
 * be used instead of a classifier.  However, when you need to return different
 * classes for instances of the same type, based on their state, you need to
 * implement a classifier.
 */
public interface Classifier<T extends Configuration>
{
    /**
     * Returns the class that the given instance belongs to.  This class can be
     * used by user interfaces to distinguish the instance (e.g. give it a
     * unique graphical representation).
     *
     * @param instance instance to classify
     * @return name of the class that the instance belongs to
     */
    String classify(T instance);
}
