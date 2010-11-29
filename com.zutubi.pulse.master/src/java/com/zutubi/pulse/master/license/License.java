package com.zutubi.pulse.master.license;

import com.zutubi.pulse.Version;
import com.zutubi.util.Constants;
import com.zutubi.util.ObjectUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * The license contains the details associated with a license, including the
 * holder of the license and the expiry date if it exists.
 *
 *
 */
public final class License
{
    private LicenseType type;
    private String holder;
    private Date expiryDate;

    public static final int UNRESTRICTED = -1;
    public static final int UNSPECIFIED  = -2;

    private int supportedProjects      = UNRESTRICTED;
    private int supportedUsers         = UNRESTRICTED;
    private int supportedAgents        = UNRESTRICTED;
    private int supportedContactPoints = UNSPECIFIED;

    public License(LicenseType type, String holder)
    {
        this(type, holder, null);
    }

    public License(LicenseType type, String holder, Date expiry)
    {
        this.type = type;
        this.holder = holder;
        this.expiryDate = expiry;
    }

    public License setSupported(int agents, int projects, int users)
    {
        setSupportedAgents(agents);
        setSupportedProjects(projects);
        setSupportedUsers(users);
        return this;
    }

    public License setSupportedAgents(int agents)
    {
        this.supportedAgents = agents;
        return this;
    }

    public License setSupportedProjects(int projects)
    {
        this.supportedProjects = projects;
        return this;
    }

    public License setSupportedUsers(int users)
    {
        this.supportedUsers = users;
        return this;
    }

    public License setSupportedContactPoints(int contactPoints)
    {
        this.supportedContactPoints = contactPoints;
        return this;
    }

    public void setExpiryDate(Date date)
    {
        this.expiryDate = date;
    }

    protected void setType(LicenseType type)
    {
        this.type = type;
    }

    /**
     * Get the license holder string.
     *
     * @return a string representing the owner of this license.
     */
    public String getHolder()
    {
        return holder;
    }

    /**
     * Get the expiry date of this license.
     *
     * @return the license expiry date.
     */
    public Date getExpiryDate()
    {
        return expiryDate;
    }

    /**
     * Get the type of this license. For example, evaluation.
     *
     * @return the name string
     */
    public LicenseType getType()
    {
        return type;
    }

    public boolean isEvaluation()
    {
        return type == LicenseType.EVALUATION;
    }
    
    /**
     * Get the number of projects supported by this license.
     *
     * @return the number of projects allowed by this license.
     */
    public int getSupportedProjects()
    {
        return supportedProjects;
    }

    /**
     * Get the number of users supported by this license.
     *
     * @return the number of projects allowed by this license.
     */
    public int getSupportedUsers()
    {
        return supportedUsers;
    }

    /**
     * Get the number of agents supported by this license.
     *
     * @return the number of agents allowed by this license.
     */
    public int getSupportedAgents()
    {
        return supportedAgents;
    }

    /**
     * Get the number of contact points per user supported by this license.
     * 
     * @return the number of contact points a user is allowed by this license.
     */
    public int getSupportedContactPoints()
    {
        // retroactively apply contact point restrictions to all of the small team licenses issues so far.
        if (supportedContactPoints == UNSPECIFIED)
        {
            if (type == LicenseType.SMALL_TEAM)
            {
                return 3;
            }
            return UNRESTRICTED;
        }
        return supportedContactPoints;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof License))
        {
            return false;
        }
        License other = (License) o;

        return ObjectUtils.equals(holder, other.holder) &&
                ObjectUtils.equals(expiryDate, other.expiryDate) &&
                ObjectUtils.equals(type, other.type) &&
                ObjectUtils.equals(supportedAgents, other.supportedAgents) &&
                ObjectUtils.equals(supportedUsers, other.supportedUsers) &&
                ObjectUtils.equals(getSupportedContactPoints(), other.getSupportedContactPoints()) &&
                ObjectUtils.equals(supportedProjects, other.supportedProjects);
    }

    /**
     * Indicates whether the license limits have been exceeded.
     *
     * @param projectCount actual number of projects
     * @param agentCount   actual number of agents
     * @param userCount    actual number of users
     * @return true if one or more of the license limits have been exceeded
     */
    public boolean isExceeded(int projectCount, int agentCount, int userCount)
    {
        return limitExceeded(projectCount, supportedProjects) || limitExceeded(agentCount, supportedAgents) || limitExceeded(userCount, supportedUsers);
    }

    private boolean limitExceeded(int count, int supported)
    {
        return supported != UNRESTRICTED && count > supported;
    }

    /**
     * Indicates if this license can be used to run the given version of
     * Pulse.  Running is allowed if either the license is not expired or
     * the license is commercial and the version was released before the
     * expiry date.
     *
     * @param version version to check (used for the release date)
     * @return true if this license allows the user to run the given version
     */
    public boolean canRunVersion(Version version)
    {
        // if it has not expired, all is well.
        if(!isExpired())
        {
            return true;
        }

        // if it has expired and its an evaluation version, no more.
        if(isEvaluation())
        {
            return false;
        }

        // if it has expired, and is not an evaluation, then we can run so long as the version
        // was released before we expired.
        return version.getReleaseDateAsDate().getTime() < expiryDate.getTime();
    }

    /**
     * @return true if the license has expired, false otherwise.
     */
    public boolean isExpired()
    {
        return expires() && getDaysRemaining() == 0;
    }

    /**
     *
     * @return true if this license expires, false otherwise.
     */
    public boolean expires()
    {
        return getExpiryDate() != null;
    }

    /**
     * Return the number of days remaining before this license expires.
     *
     * @return the number in days before this license expires. It will return
     * 0 if this license has expired, and -1 if it never expires.
     */
    public int getDaysRemaining()
    {
        if (expires())
        {
            return calculateDaysRemaining(Calendar.getInstance().getTime(), getExpiryDate());
        }
        return -1;
    }

    /**
     * Calculate the number of days remaining before the license expires. If the now date
     * falls on the same day as the expiry date, then we indicate 1 day as remaining. Expiry
     * occurs at midnight.
     *
     * @param now    the date to calculate from
     * @param expiry the date the license expires
     * @return the number of days between now and the expiry day (part days
     *         count as one)
     */
    static int calculateDaysRemaining(Date now, Date expiry)
    {
        // Make this static to allow testing.

        // normalise the expiry date to midnight on the day of expiry.

        Calendar x = Calendar.getInstance();
        x.setTime(expiry);

        // add one day, and zero out the rest of the details. We expire
        // at midnight.
        x.add(Calendar.DAY_OF_YEAR, 1);
        x.set(Calendar.HOUR_OF_DAY, 0);
        x.set(Calendar.MINUTE, 0);
        x.set(Calendar.SECOND, 0);
        x.set(Calendar.MILLISECOND, 0);

        int daysRemaining = 0;

        long timeRemainingInMilliSeconds = x.getTimeInMillis() - now.getTime();
        if (timeRemainingInMilliSeconds > 0)
        {
            // part days count as 1.
            daysRemaining = (int)(timeRemainingInMilliSeconds / Constants.DAY);
            if (timeRemainingInMilliSeconds % Constants.DAY > 0)
            {
                daysRemaining++;
            }
        }

        return daysRemaining;
    }
}
