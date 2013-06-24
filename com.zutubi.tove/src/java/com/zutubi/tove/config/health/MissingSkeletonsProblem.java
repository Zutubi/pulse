package com.zutubi.tove.config.health;

/**
 * Identifies that skeleton records are missing.  This occurs where records are
 * present in the template parent, but absent in the child (and not hidden).
 */
public class MissingSkeletonsProblem extends MismatchedTemplateStructureProblem
{
    /**
     * Creates a new problem indicating missing skeletons at the given key of
     * the given path.
     *
     * @param path               path of the record the skeletons should be
     *                           under
     * @param message            description of this problem
     * @param key                key where the skeletons should be
     * @param templateParentPath path of the template parent of the record the
     *                           skeletons should be under
     */
    public MissingSkeletonsProblem(String path, String message, String key, String templateParentPath)
    {
        super(path, message, key, templateParentPath);
    }

}
