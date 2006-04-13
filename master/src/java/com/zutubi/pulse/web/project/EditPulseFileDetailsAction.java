/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.PulseFileDetails;
import com.zutubi.pulse.model.persistence.PulseFileDetailsDao;

/**
 *
 *
 */
public class EditPulseFileDetailsAction extends ProjectActionSupport
{
    private long id;
    private PulseFileDetailsDao pulseFileDetailsDao;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String doInput()
    {
        PulseFileDetails details = pulseFileDetailsDao.findById(id);
        if (details == null)
        {
            addActionError("Unknown project details '" + id + "'");
            return ERROR;
        }

        return details.getType();
    }

    public String execute()
    {
        return SUCCESS;
    }

    public void setPulseFileDetailsDao(PulseFileDetailsDao pulseFileDetailsDao)
    {
        this.pulseFileDetailsDao = pulseFileDetailsDao;
    }
}
