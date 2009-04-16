package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.scm.api.EOLStyle;
import com.zutubi.pulse.core.scm.api.FileStatus;
import com.zutubi.pulse.core.scm.api.PersonalBuildUI;
import com.zutubi.pulse.core.scm.api.WorkingCopyStatus;
import static com.zutubi.pulse.core.scm.p4.PerforceConstants.*;

/**
 * A handler for p4 fstat output that builds up a working copy status.
 */
public class StatusBuildingFStatHandler extends AbstractPerforceFStatHandler
{
    private WorkingCopyStatus status;

    public StatusBuildingFStatHandler(PersonalBuildUI ui, WorkingCopyStatus status)
    {
        super(ui);
        this.status = status;
    }

    protected void handleCurrentItem()
    {
        if(currentItem.containsKey(FSTAT_CLIENT_FILE))
        {
            String path = getPath(currentItem.get(FSTAT_CLIENT_FILE));
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

            FileStatus fs = new FileStatus(path, state, false);

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

    private String getPath(String clientFile)
    {
        // clientFile has form //<client>/<path>
        int length = clientFile.length();
        if(length > 3)
        {
            int index = clientFile.indexOf('/', 2);
            if(index >= 0 && index < length - 1)
            {
                clientFile = clientFile.substring(index + 1);
            }
        }

        return clientFile;
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
