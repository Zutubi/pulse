package com.zutubi.pulse.core.scm.patch;

import com.zutubi.pulse.core.scm.patch.api.PatchFormat;
import com.zutubi.util.Pair;

import java.io.File;
import java.util.List;

/**
 * An interface for factories that can create {@link com.zutubi.pulse.core.scm.patch.api.PatchFormat}
 * instances based on types.
 */
public interface PatchFormatFactory
{
    /**
     * Indicates if the given format type is recognised.
     *
     * @param formatType the type to check
     * @return true if the type is recognised, false otherwise
     */
    boolean isValidFormatType(String formatType);

    /**
     * Returns a list of all known format types.
     *
     * @return all known format types
     */
    List<String> getFormatTypes();

    /**
     * Creates a patch format to use with SCMs of the given type.  This is
     * used on the working copy side where we identify the type of SCM from the
     * project and use that knowledge to decide the patch format to choose.
     *
     * @param scmType type of SCM to get the patch format for
     * @return a pair containing the patch format type and instance for the
     *         SCM, may be null if the SCM has not registered a valid patch
     *         format type
     */
    Pair<String, PatchFormat> createByScmType(String scmType);

    /**
     * Creates a patch format of the given type.  This is the type used to
     * register the format extension.  Used on the server side where personal
     * build requests come in with their patch type specified.
     *
     * @param formatType type of patch format to create
     * @return a patch format implementation of the given type, may be null if
     *         the type is unrecognised
     */
    PatchFormat createByFormatType(String formatType);

    /**
     * Attempts to guess the type of a patch file.  The guess is deferred to
     * the various formats.  If only one format claims it, that guess is
     * returned.  If none or multiple claim it, null is returned.
     *
     * @param patchFile file to guess the format type for
     * @return a guess of the format type for the patch, or null if no
     *         unambiguous guess can be made
     */
    String guessFormatType(File patchFile);
}
