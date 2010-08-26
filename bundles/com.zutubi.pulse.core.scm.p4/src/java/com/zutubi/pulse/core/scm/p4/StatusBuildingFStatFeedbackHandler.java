package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.scm.api.EOLStyle;
import com.zutubi.pulse.core.scm.patch.api.FileStatus;
import com.zutubi.pulse.core.scm.patch.api.WorkingCopyStatus;
import com.zutubi.pulse.core.ui.api.UserInterface;

import static com.zutubi.pulse.core.scm.p4.PerforceConstants.*;

/**
 * A handler for p4 fstat output that builds up a working copy status.
 */
public class StatusBuildingFStatFeedbackHandler extends AbstractPerforceFStatFeedbackHandler
{
    private UserInterface ui;
    private WorkingCopyStatus status;

    public StatusBuildingFStatFeedbackHandler(UserInterface ui, WorkingCopyStatus status)
    {
        super();
        this.ui = ui;
        this.status = status;
    }

    protected void handleCurrentItem()
    {
        if(currentItem.containsKey(FSTAT_CLIENT_FILE))
        {
            String path = PerforceCore.stripClientPrefix(currentItem.get(FSTAT_CLIENT_FILE));
            String depotPath = currentItem.get(FSTAT_DEPOT_FILE);
            String action = currentItem.get(FSTAT_ACTION);
            FileStatus.State state = FileStatus.State.UNCHANGED;

            if(currentItem.containsKey(FSTAT_UNRESOLVED))
            {
                state = FileStatus.State.UNRESOLVED;
            }
            else if(action != null)
            {
                state = mapAction(action);
                String have = currentItem.get(FSTAT_HAVE_REVISION);
                if(have != null && have.equals(REVISION_NONE))
                {
                    if(state != FileStatus.State.DELETED)
                    {
                        ui.warning("Change to deleted file '" + path + "'");
                        state = FileStatus.State.UNRESOLVED;
                    }
                }
            }

            FileStatus fs = new FileStatus(path, state, false, depotPath);

            if(fs.isInteresting())
            {
                if(ui != null)
                {
                    ui.status(fs.toString());
                }

                if(fs.getState().preferredPayloadType() != FileStatus.PayloadType.NONE)
                {
                    String type = getCurrentItemType();
                    String headType = getCurrentItemHeadType();

                    if(fileIsText(type))
                    {
                        fs.setProperty(FileStatus.PROPERTY_EOL_STYLE, EOLStyle.TEXT.toString());
                    }

                    resolveExecutableProperty(fs, type, headType);
                }

                status.addFileStatus(fs);
            }
        }
    }

    private void resolveExecutableProperty(FileStatus fs, String type, String headType)
    {
        if(!type.equals(headType))
        {
            if(fileIsExecutable(type))
            {
                if(!fileIsExecutable(headType))
                {
                    fs.setProperty(FileStatus.PROPERTY_EXECUTABLE, "true");
                }
            }
            else
            {
                if(fileIsExecutable(headType))
                {
                    fs.setProperty(FileStatus.PROPERTY_EXECUTABLE, "false");
                }
            }
        }
    }

    private FileStatus.State mapAction(String action)
    {
        if(action.equals(ACTION_ADD))
        {
            return FileStatus.State.ADDED;
        }
        else if (action.equals(ACTION_BRANCH))
        {
            return FileStatus.State.BRANCHED;
        }
        else if (action.equals(ACTION_DELETE))
        {
            return FileStatus.State.DELETED;
        }
        else if (action.equals(ACTION_EDIT))
        {
            return FileStatus.State.MODIFIED;
        }
        else if (action.equals(ACTION_INTEGRATE))
        {
            return FileStatus.State.MERGED;
        }
        else
        {
            ui.warning("Unrecognised action '" + action + "': assuming file is modified.");
            return FileStatus.State.MODIFIED;
        }
    }

    private boolean fileIsExecutable(String type)
    {
        int plusIndex = type.indexOf('+');
        if(plusIndex >= 0)
        {
            for(int i = plusIndex + 1; i < type.length(); i++)
            {
                if(type.charAt(i) == 'x')
                {
                    return true;
                }
            }
        }
        else
        {
            for(String exe: EXECUTABLE_TYPES)
            {
                if(type.equals(exe))
                {
                    return true;
                }
            }
        }

        return false;
    }
}
