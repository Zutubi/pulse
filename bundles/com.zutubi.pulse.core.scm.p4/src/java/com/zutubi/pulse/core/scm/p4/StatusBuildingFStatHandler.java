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

    protected void handleItem()
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
                        warning("Change to deleted file '" + path + "'");
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
            warning("Unrecognised action '" + action + "': assuming file is modified.");
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

    // Run: p4 fstat -Op -Rc //...@revision
    //
    // Summary (so far):
    //   - file is changed on client if we have an [action] (which also
    //     denotes the type of the change)
    //   - file is out of date on client if the [headRev] is different
    //     to the [haveRev], or there is no [haveRev]
    //   - file is in an inconsistent state if we have an [unresolved]:
    //     i.e. a merge (possibly just a sync) has not been resolved
    //
    // Full details:
    //
    // Normal item:
    // ... depotFile //depot/script1
    // ... clientFile /home/jason/p41\script1
    // ... isMapped
    // ... headAction add
    // ... headType xtext
    // ... headTime 1160291666
    // ... headRev 1
    // ... headChange 1
    // ... headModTime 1160295147
    // ... haveRev 1
    //
    // Open for edit:
    // ... depotFile //depot/script1
    // ... clientFile /home/jason/p41\script1
    // ... isMapped
    // ... headAction add
    // ... headType xtext
    // ... headTime 1160291666
    // ... headRev 1
    // ... headChange 1
    // ... headModTime 1160295147
    // ... haveRev 1
    // ... action edit
    // ... change default
    // ... type xtext
    // ... actionOwner test-user
    //
    // Open for add:
    // ... depotFile //depot/newfile
    // ... clientFile /home/jason/p41\newfile
    // ... action add
    // ... change default
    // ... type text
    // ... actionOwner test-user
    //
    // Open for delete:
    // ... depotFile //depot/script1
    // ... clientFile /home/jason/p41\script1
    // ... isMapped
    // ... headAction add
    // ... headType xtext
    // ... headTime 1160291666
    // ... headRev 1
    // ... headChange 1
    // ... headModTime 1160295147
    // ... haveRev 1
    // ... action delete
    // ... change default
    // ... type xtext
    // ... actionOwner test-user
    //
    // Open for edit (change type):
    // ... depotFile //depot/file5
    // ... clientFile /home/jason/p41\file5
    // ... isMapped
    // ... headAction add
    // ... headType text
    // ... headTime 1160291666
    // ... headRev 1
    // ... headChange 1
    // ... headModTime 1160294903
    // ... haveRev 1
    // ... action edit
    // ... change default
    // ... type xtext
    // ... actionOwner test-user
    //
    // Integrate new file:
    // ... depotFile //depot/script2
    // ... clientFile /home/jason/p41\script2
    // ... action branch
    // ... change default
    // ... type xtext
    // ... actionOwner test-user
    // ... resolved
    //
    // Integrate (merge) from branch:
    // ... depotFile //depot/file1
    // ... clientFile /home/jason/p41\file1
    // ... isMapped
    // ... headAction add
    // ... headType text
    // ... headTime 1160291666
    // ... headRev 1
    // ... headChange 1
    // ... headModTime 1160294897
    // ... haveRev 1
    // ... action integrate
    // ... change default
    // ... type text
    // ... actionOwner test-user
    // ... unresolved
    //
    // New file in repo (not yet sync'd):
    // ... depotFile //depot/newfile
    // ... clientFile /home/jason/p41\newfile
    // ... isMapped
    // ... headAction add
    // ... headType text
    // ... headTime 1160307400
    // ... headRev 1
    // ... headChange 4
    // ... headModTime 1160310899
    //
    // File deleted in repo (not yet sync'd):
    // ... depotFile //depot/file1
    // ... clientFile /home/jason/p41\file1
    // ... isMapped
    // ... headAction delete
    // ... headType text
    // ... headTime 1160307528
    // ... headRev 2
    // ... headChange 5
    // ... headModTime 0
    // ... haveRev 1
}
