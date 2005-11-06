package com.cinnamonbob.web.project;

import com.cinnamonbob.model.P4;

/**
 *
 *
 */
public class EditP4Action extends AbstractEditScmAction
{
    private P4 scm = new P4();

    public P4 getScm()
    {
        return scm;
    }

    public String getScmProperty()
    {
        return "p4";
    }

    public P4 getP4()
    {
        return getScm();
    }

    public String doDefault()
    {
        scm = (P4) getScmManager().getScm(getId());
        return SUCCESS;
    }

    public String execute()
    {
        P4 persistentP4 = (P4) getScmManager().getScm(getId());
        persistentP4.setName(scm.getName());
        persistentP4.setPassword(scm.getPassword());
        persistentP4.setPath(scm.getPath());
        persistentP4.setClient(scm.getClient());
        persistentP4.setPort(scm.getPort());
        persistentP4.setUser(scm.getUser());

        return SUCCESS;
    }
}
