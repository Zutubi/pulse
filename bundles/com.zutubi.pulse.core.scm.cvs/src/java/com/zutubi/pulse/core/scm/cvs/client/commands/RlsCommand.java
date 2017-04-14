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

package com.zutubi.pulse.core.scm.cvs.client.commands;

import org.netbeans.lib.cvsclient.ClientServices;
import org.netbeans.lib.cvsclient.command.BasicCommand;
import org.netbeans.lib.cvsclient.command.Builder;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.event.EventManager;
import org.netbeans.lib.cvsclient.request.ArgumentRequest;
import org.netbeans.lib.cvsclient.request.CommandRequest;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * 
 */
public class RlsCommand extends BasicCommand
{
    public static CommandRequest RLS;

    static
    {
        try
        {
            // time to create a command request with the 'rls\n' string.
            // Unfortunately, the command request has a private constructor, so
            // this is necessary.
            Constructor c = CommandRequest.class.getDeclaredConstructor(String.class);
            c.setAccessible(true);
            RLS = (CommandRequest) c.newInstance("rlist\n");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private boolean displayInEntriesFormat;

    private boolean displayAllDetails;

    private boolean showDeadRevisions;

    private boolean hideEmptyDirectories;

    private String[] paths;

    public RlsCommand()
    {
        resetCVSCommand();
    }

    public void execute(ClientServices client, EventManager em) throws CommandException, AuthenticationException
    {
        client.ensureConnection();
        super.execute(client, em);

        try
        {
            addArgumentRequest(isDisplayAllDetails(), "-l");
            addArgumentRequest(isDisplayInEntriesFormat(), "-e");
            addArgumentRequest(isShowDeadRevisions(), "-d");
            addArgumentRequest(isHideEmptyDirectories(), "-P");
            addArgumentRequest(isRecursive(), "-R");

            for (String path : paths)
            {
                requests.add(new ArgumentRequest(path));
            }

            addRequest(RLS);

            client.processRequests(requests);
            requests.clear();
        }
        catch (CommandException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new CommandException(ex, ex.getLocalizedMessage());
        }
    }

    /**
     * Don't send status of local files prior to executing command, as it's not
     * needed.
     */
    protected boolean assumeLocalPathWhenUnspecified()
    {
        return false;
    }

    public String getCVSCommand()
    {
        StringBuffer toReturn = new StringBuffer("rlist ");
        toReturn.append(getCVSArguments());
        if (paths != null)
        {
            for (String path : paths)
            {
                toReturn.append(path);
                toReturn.append(' ');
            }
        }
        return toReturn.toString();
    }

    public String getCVSArguments()
    {
        StringBuffer toReturn = new StringBuffer(""); //NOI18N
        if (displayAllDetails)
        {
            toReturn.append("-l "); //NOI18N
        }
        if (isRecursive())
        {
            toReturn.append("-R "); //NOI18N
        }
        if (displayInEntriesFormat)
        {
            toReturn.append("-e "); //NOI18N
        }
        if (showDeadRevisions)
        {
            toReturn.append("-d "); //NOI18N
        }
        if (hideEmptyDirectories)
        {
            toReturn.append("-P "); //NOI18N
        }
        return toReturn.toString();
    }

    /**
     * The Version command does not support any options.
     *
     * @param opt
     * @param optArg
     * @return false since this command does not support any options.
     */
    public boolean setCVSCommand(char opt, String optArg)
    {
        // no options to set.
        if (opt == 'e')
        {
            setDisplayInEntriesFormat(true);
        }
        else if (opt == 'R')
        {
            setRecursive(true);
        }
        else if (opt == 'l')
        {
            setDisplayAllDetails(true);
        }
        else if (opt == 'd')
        {
            setShowDeadRevisions(true);
        }
        else if (opt == 'P')
        {
            setHideEmptyDirectories(true);
        }
        else
        {
            return false;
        }
        return true;
    }

    /**
     * Reset this CVS command instance.
     */
    public void resetCVSCommand()
    {
        setRecursive(false);
        setDisplayAllDetails(false);
        setDisplayInEntriesFormat(false);
        setShowDeadRevisions(false);
        setHideEmptyDirectories(false);
        setPaths();
    }

    /**
     * Get the list of options that are allowed for this command. In the case of
     * this command, we return an empty string since the version command does not
     * support any options.
     *
     * @return a single of letters that represent the allowable command options.
     */
    public String getOptString()
    {
        return "RledP";
    }

    public Builder createBuilder(EventManager eventMan)
    {
        RlsBuilder builder = new RlsBuilder();
        builder.setPaths(paths);
        return builder;
    }

    public void setPaths(String... paths)
    {
        this.paths = paths;
    }

    /**
     * Display in CVS/Entries format. This format is meant to remain easily parsable by automation.
     *
     * @param b is true, then the result is displayed in CVS/Entries format
     */
    public void setDisplayInEntriesFormat(boolean b)
    {
        this.displayInEntriesFormat = b;
    }

    public boolean isDisplayInEntriesFormat()
    {
        return displayInEntriesFormat;
    }

    /**
     * Show all details
     *
     * @param b is true, then all details are displayed.
     */
    public void setDisplayAllDetails(boolean b)
    {
        this.displayAllDetails = b;
    }

    public boolean isDisplayAllDetails()
    {
        return displayAllDetails;
    }

    /**
     * Show dead revisions (with tag when specified).
     *
     * @param b is true, then dead revisions are shown.
     */
    public void setShowDeadRevisions(boolean b)
    {
        this.showDeadRevisions = b;
    }

    public boolean isShowDeadRevisions()
    {
        return showDeadRevisions;
    }

    /**
     * Don't list contents of empty directories when recursing.
     *
     * @param b is true, empty directories are not shown.
     */
    public void setHideEmptyDirectories(boolean b)
    {
        this.hideEmptyDirectories = b;
    }

    public boolean isHideEmptyDirectories()
    {
        return hideEmptyDirectories;
    }

    public List<RlsInfo> getListing()
    {
        return ((RlsBuilder)builder).getListing();
    }
}
