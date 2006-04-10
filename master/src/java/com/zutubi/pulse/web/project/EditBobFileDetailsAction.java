package com.zutubi.pulse.web.project;

import com.zutubi.pulse.model.BobFileDetails;
import com.zutubi.pulse.model.persistence.BobFileDetailsDao;

/**
 *
 *
 */
public class EditBobFileDetailsAction extends ProjectActionSupport
{
    private long id;
    private BobFileDetailsDao bobFileDetailsDao;

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
        BobFileDetails details = bobFileDetailsDao.findById(id);
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

    public void setBobFileDetailsDao(BobFileDetailsDao bobFileDetailsDao)
    {
        this.bobFileDetailsDao = bobFileDetailsDao;
    }
}
