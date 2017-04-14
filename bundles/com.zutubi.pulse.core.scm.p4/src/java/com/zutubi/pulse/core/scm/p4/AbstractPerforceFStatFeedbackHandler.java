/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.pulse.core.scm.p4;

import com.zutubi.pulse.core.scm.api.ScmException;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base for handlers which deal with p4 fstat output.
 * <p/>
 * Run: p4 fstat -Op -Rc //...@revision
 * <p/>
 * Summary (so far):
 * <ul>
 *   <li>file is changed on client if we have an [action] (which also
 *       denotes the type of the change)</li>
 *   <li>file is out of date on client if the [headRev] is different
 *       to the [haveRev], or there is no [haveRev]</li>
 *   <li>file is in an inconsistent state if we have an [unresolved]:
 *       i.e. a merge (possibly just a sync) has not been resolved</li>
 * </ul>
 * Full details:
 * <pre>
 * Normal item:
 * ... depotFile //depot/script1
 * ... clientFile /home/jason/p41\script1
 * ... isMapped
 * ... headAction add
 * ... headType xtext
 * ... headTime 1160291666
 * ... headRev 1
 * ... headChange 1
 * ... headModTime 1160295147
 * ... haveRev 1
 *
 * Open for edit:
 * ... depotFile //depot/script1
 * ... clientFile /home/jason/p41\script1
 * ... isMapped
 * ... headAction add
 * ... headType xtext
 * ... headTime 1160291666
 * ... headRev 1
 * ... headChange 1
 * ... headModTime 1160295147
 * ... haveRev 1
 * ... action edit
 * ... change default
 * ... type xtext
 * ... actionOwner test-user
 *
 * Open for add:
 * ... depotFile //depot/newfile
 * ... clientFile /home/jason/p41\newfile
 * ... action add
 * ... change default
 * ... type text
 * ... actionOwner test-user
 *
 * Open for delete:
 * ... depotFile //depot/script1
 * ... clientFile /home/jason/p41\script1
 * ... isMapped
 * ... headAction add
 * ... headType xtext
 * ... headTime 1160291666
 * ... headRev 1
 * ... headChange 1
 * ... headModTime 1160295147
 * ... haveRev 1
 * ... action delete
 * ... change default
 * ... type xtext
 * ... actionOwner test-user
 *
 * Open for edit (change type):
 * ... depotFile //depot/file5
 * ... clientFile /home/jason/p41\file5
 * ... isMapped
 * ... headAction add
 * ... headType text
 * ... headTime 1160291666
 * ... headRev 1
 * ... headChange 1
 * ... headModTime 1160294903
 * ... haveRev 1
 * ... action edit
 * ... change default
 * ... type xtext
 * ... actionOwner test-user
 *
 * Integrate new file:
 * ... depotFile //depot/script2
 * ... clientFile /home/jason/p41\script2
 * ... action branch
 * ... change default
 * ... type xtext
 * ... actionOwner test-user
 * ... resolved
 *
 * Integrate (merge) from branch:
 * ... depotFile //depot/file1
 * ... clientFile /home/jason/p41\file1
 * ... isMapped
 * ... headAction add
 * ... headType text
 * ... headTime 1160291666
 * ... headRev 1
 * ... headChange 1
 * ... headModTime 1160294897
 * ... haveRev 1
 * ... action integrate
 * ... change default
 * ... type text
 * ... actionOwner test-user
 * ... unresolved
 *
 * New file in repo (not yet sync'd):
 * ... depotFile //depot/newfile
 * ... clientFile /home/jason/p41\newfile
 * ... isMapped
 * ... headAction add
 * ... headType text
 * ... headTime 1160307400
 * ... headRev 1
 * ... headChange 4
 * ... headModTime 1160310899
 *
 * File deleted in repo (not yet sync'd):
 * ... depotFile //depot/file1
 * ... clientFile /home/jason/p41\file1
 * ... isMapped
 * ... headAction delete
 * ... headType text
 * ... headTime 1160307528
 * ... headRev 2
 * ... headChange 5
 * ... headModTime 0
 * ... haveRev 1
 * </pre>
 */
public abstract class AbstractPerforceFStatFeedbackHandler extends PerforceErrorDetectingFeedbackHandler
{
    protected Map<String, String> currentItem = new HashMap<String, String>();

    public AbstractPerforceFStatFeedbackHandler()
    {
        super(true);
    }

    public void handleStdout(String line)
    {
        line = line.trim();
        if(line.length() == 0)
        {
            if(currentItem.size() > 0)
            {
                handleCurrentItem();
                currentItem.clear();
            }
        }
        else
        {
            String[] parts = line.split(" ", 3);
            if(parts.length == 3)
            {
                currentItem.put(parts[1], parts[2]);
            }
            else if(parts.length == 2)
            {
                currentItem.put(parts[1], "");
            }
        }
    }

    public void handleStderr(String line)
    {
        // Filter out spurious error (nothing changed)
        if(!line.contains("file(s) not opened on this client") && !line.equals("//... - no such file(s)."))
        {
            super.handleStderr(line);
        }
    }

    public void handleExitCode(int code) throws ScmException
    {
        super.handleExitCode(code);
        if(currentItem.size() > 0)
        {
            handleCurrentItem();
        }
    }

    protected String getCurrentItemType()
    {
        String type = currentItem.get(com.zutubi.pulse.core.scm.p4.PerforceConstants.FSTAT_TYPE);
        if(type == null)
        {
            type = getCurrentItemHeadType();
        }
        return type;
    }

    protected String getCurrentItemHeadType()
    {
        String type = currentItem.get(com.zutubi.pulse.core.scm.p4.PerforceConstants.FSTAT_HEAD_TYPE);
        if(type == null)
        {
            type = "text";
        }

        return type;
    }

    protected boolean fileIsText(String type)
    {
        return type.contains("text");
    }

    protected abstract void handleCurrentItem();
}
