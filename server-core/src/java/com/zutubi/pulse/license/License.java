package com.zutubi.pulse.license;

import com.zutubi.pulse.util.Constants;
import com.zutubi.pulse.util.ObjectUtils;

import java.util.Date;
import java.util.Calendar;

/**
 * The license contains the details associated with a license, including the
 * holder of the license and the expiry date if it exists.
 *
 *
 */
public class License
{
    private String name;
    private String holder;
    private Date expiryDate;

    public License(String name, String holder, Date expiry)
    {
        this.name = name;
        this.holder = holder;
        this.expiryDate = expiry;
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
     * Get the name of this license. For example, evaluation.
     *
     * @return the name string
     */
    public String getName()
    {
        return name;
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
                ObjectUtils.equals(name, other.name);
    }


    /**
     *
     * @return true if the license has expired, false otherwise.
     */
    public boolean hasExpired()
    {
        if (expires())
        {
            return getDaysRemaining() == 0;
        }
        return false;
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
