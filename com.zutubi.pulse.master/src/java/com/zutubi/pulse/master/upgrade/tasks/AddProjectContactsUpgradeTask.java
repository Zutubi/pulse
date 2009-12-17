package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.MutableRecordImpl;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.util.UnaryFunction;

/**
 * Adds the new "contacts" section to project configuration.
 */
public class AddProjectContactsUpgradeTask extends AbstractUpgradeTask
{
    private static final String SCOPE_PROJECTS = "projects";

    private static final String TYPE_CONTACTS = "zutubi.projectContactsConfig";

    private static final String PROPERTY_USERS = "users";
    private static final String PROPERTY_GROUPS = "groups";
    private static final String PROPERTY_CONTACTS = "contacts";

    private RecordManager recordManager;

    public boolean haltOnFailure()
    {
        return true;
    }

    public void execute() throws TaskException
    {
        TemplatedScopeDetails details = new TemplatedScopeDetails(SCOPE_PROJECTS, recordManager);
        details.getHierarchy().forEach(new UnaryFunction<ScopeHierarchy.Node, Boolean>()
        {
            public Boolean process(ScopeHierarchy.Node node)
            {
                MutableRecord contactsRecord = new MutableRecordImpl();
                contactsRecord.setSymbolicName(TYPE_CONTACTS);
                if (node.getParent() == null)
                {
                    contactsRecord.put(PROPERTY_USERS, new String[0]);
                    contactsRecord.put(PROPERTY_GROUPS, new String[0]);
                }
                recordManager.insert(PathUtils.getPath(SCOPE_PROJECTS, node.getId(), PROPERTY_CONTACTS), contactsRecord);
                return true;
            }
        });
    }

    public void setRecordManager(RecordManager recordManager)
    {
        this.recordManager = recordManager;
    }
}
