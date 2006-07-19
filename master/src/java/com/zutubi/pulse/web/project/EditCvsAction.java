package com.zutubi.pulse.web.project;

import com.opensymphony.util.TextUtils;
import com.zutubi.pulse.model.Cvs;

/**
 *
 *
 */
public class EditCvsAction extends AbstractEditScmAction
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
        super.prepare();

        scm = (Cvs) getScmManager().getScm(getId());

        minutes = scm.getQuietPeriodMinutes();
        seconds = scm.getQuietPeriodSeconds();
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
                    addFieldError("quiet", getText("unit.invalid.negative"));
                    return;
                }
            }

            // check the seconds field.
            if (TextUtils.stringSet(seconds))
            {
                if (Integer.parseInt(seconds) < 0)
                {
                    addFieldError("quiet", getText("unit.invalid.negative"));
                }
            }
        }
        catch (NumberFormatException nfe)
        {
            addFieldError("quiet", getText("unit.invalid.nan"));
        }
    }

    public String execute()
    {
        getCvs().setQuietPeriod(minutes, seconds);

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
