package com.cinnamonbob.web.project;

import com.cinnamonbob.model.Cvs;
import com.cinnamonbob.core.util.Constants;
import com.opensymphony.xwork.Preparable;
import com.opensymphony.util.TextUtils;

/**
 *
 *
 */
public class EditCvsAction extends AbstractEditScmAction implements Preparable
{
    private Cvs scm = new Cvs();

    private String minutes;
    private String seconds;

    public Cvs getScm()
    {
        return scm;
    }

    public String getScmProperty()
    {
        return "cvs";
    }

    public Cvs getCvs()
    {
        return getScm();
    }

    public void prepare() throws Exception
    {
        scm = (Cvs) getScmManager().getScm(getId());

        long quietPeriod = scm.getQuietPeriod();
        long mins = (quietPeriod / Constants.MINUTE);
        if (mins > 0)
        {
            minutes = Long.toString(mins);
        }
        long secs = (quietPeriod % Constants.MINUTE) / Constants.SECOND;
        if (secs > 0)
        {
            seconds = Long.toString(secs);
        }
    }

    public void validate()
    {
        try
        {
            // check the minutes field.
            if (TextUtils.stringSet(minutes))
            {
                if (Integer.parseInt(minutes) < 0)
                {
                    addFieldError("quiet", "unit.invalid.negative");
                    return;
                }
            }

            // check the seconds field.
            if (TextUtils.stringSet(seconds))
            {
                if (Integer.parseInt(seconds) < 0)
                {
                    addFieldError("quiet", "unit.invalid.negative");
                }
            }
        }
        catch (NumberFormatException nfe)
        {
            addFieldError("quiet", "unit.invalid.nan");
        }
    }

    public String execute()
    {
        // convert the mins / secs to long and set.
        long quietPeriod = 0;
        if (TextUtils.stringSet(minutes))
        {
            quietPeriod += Integer.parseInt(minutes) * Constants.MINUTE;
        }
        if (TextUtils.stringSet(seconds))
        {
            quietPeriod += Integer.parseInt(seconds) * Constants.SECOND;
        }
        getScm().setQuietPeriod(quietPeriod);

        return super.execute();
    }

    public String getMinutes()
    {
        return minutes;
    }

    public void setMinutes(String minutes)
    {
        this.minutes = minutes;
    }

    public String getSeconds()
    {
        return seconds;
    }

    public void setSeconds(String seconds)
    {
        this.seconds = seconds;
    }
}
