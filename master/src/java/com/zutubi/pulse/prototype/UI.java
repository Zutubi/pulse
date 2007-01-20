package com.zutubi.pulse.prototype;

import com.zutubi.pulse.prototype.record.Record;

/**
 * <class comment/>
 */
public class UI
{
    private long projectId;
    
    private TemplateManager templateManager;
    private RecordTypeRegistry typeRegistry;
    private ScmManager scmManager;

    public void doSomething()
    {
        TemplateRecord scm = templateManager.load(Scopes.PROJECTS, Long.toString(projectId), "scm");

        //
        Class scmConfigType = typeRegistry.getType(scm.getSymbolicName());


        // scmConfigType used as definition for form.
        String renderedFormHtml = "";

        Record specifics = templateManager.load(Scopes.PROJECTS, Long.toString(projectId), "basics");

    }

}
