/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.core.scm.api;

/**
 * Represents an atomic revision in an SCM server.  Different SCMs denote
 * revisions in different ways.  For example, in Subversion revisions are
 * simply numbers, but in Git a unique hash is used.  Each implementation is
 * responsible for being able to serialise revisions to a simple string form.
 */
public class Revision
{
    /**
     * Represents whatever the latest revision is at the time.
     */
    public static final Revision HEAD = null;
    /**
     * Maximum length of a non-numeric revision string before it will be
     * abbreviated.
     */
    public static final int ABBREVIATION_LIMIT = 9;
    public static final String ELLIPSIS = "...";

    private String revisionString;

    // Used by hessian
    Revision()
    {
    }
    
    /**
     * Creates a new revision from the serialised form.
     *
     * @param revisionString a serialised form of the revision, the content of
     *                       which is implementation-dependent
     * @throws NullPointerException if revisionString is null
     */
    public Revision(String revisionString)
    {
        if (revisionString == null)
        {
            throw new NullPointerException("Revision string may not be null");
        }

        this.revisionString = revisionString;
    }

    /**
     * Creates a new revision based in the given revision number.  This is a
     * convenience constructor for those scms that use numeric revisions.
     *
     * @param revision a numeric revision string.
     */
    public Revision(long revision)
    {
        this.revisionString = Long.toString(revision);
    }

    /**
     * @return a serialised version of the revision
     */
    public String getRevisionString()
    {
        return revisionString;
    }

    /**
     * Convenience method to return the previous revision when this revision is
     * numerical.
     *
     * @return the previous numerical revision, or null if this is revision 1
     * @throws NumberFormatException if our revision string cannot be parsed as
     *         a lon 
     */
    public Revision calculatePreviousNumericalRevision()
    {
        long number = Long.parseLong(revisionString);
        if(number > 0)
        {
            return new Revision(String.valueOf(number - 1));
        }
        return null;
    }

    /**
     * Tests if this revision is a simple number.
     *
     * @return true iff the revision is a simple number
     */
    public boolean isNumeric()
    {
        try
        {
            Long.parseLong(revisionString);
            return true;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    /**
     * Indicates if the revision string has an abbreviated form that differs
     * from its full form (i.e. {@link #getRevisionString()} returns a
     * different value to {@link #getAbbreviatedRevisionString()}).
     *
     * @return true iff this revision has an abbreviated form
     */
    public boolean isAbbreviated()
    {
        return !isNumeric() && revisionString.length() > ABBREVIATION_LIMIT;
    }

    /**
     * Returns an abbreviated form of the revision string.  This will be
     * identical to the full form unless {@link #isAbbreviated()} returns
     * true, in which case it will be no longer than
     * {@link #ABBREVIATION_LIMIT}.
     *
     * @return an abbreviated form of the revision string
     */
    public String getAbbreviatedRevisionString()
    {
        if (isAbbreviated())
        {
            return revisionString.substring(0, ABBREVIATION_LIMIT - ELLIPSIS.length()) + ELLIPSIS;
        }
        else
        {
            return revisionString;
        }
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        Revision revision = (Revision) o;
        return revisionString.equals(revision.revisionString);
    }

    public int hashCode()
    {
        return revisionString.hashCode();
    }

    public String toString()
    {
        return revisionString;
    }
}
