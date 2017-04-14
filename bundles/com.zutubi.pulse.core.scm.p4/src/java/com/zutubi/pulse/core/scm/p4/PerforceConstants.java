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

import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.util.StringUtils;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Shared constants used in Perforce implementations.
 */
public class PerforceConstants
{
    public static final String PROPERTY_PORT = "p4.port";
    public static final String PROPERTY_CLIENT = "p4.client";
    public static final String PROPERTY_USER = "p4.user";
    public static final String PROPERTY_PASSWORD = "p4.password";

    public static final String ENV_CHARSET = "P4CHARSET";
    public static final String ENV_DIFF = "P4DIFF";
    public static final String ENV_PORT = "P4PORT";
    public static final String ENV_USER = "P4USER";
    public static final String ENV_PASSWORD = "P4PASSWD";
    public static final String ENV_CLIENT = "P4CLIENT";
    public static final String COMMAND_ADD = "add";
    public static final String COMMAND_CHANGE = "change";
    public static final String COMMAND_CHANGES = "changes";
    public static final String COMMAND_CLIENT = "client";
    public static final String COMMAND_CLIENTS = "clients";
    public static final String COMMAND_DELETE = "delete";
    public static final String COMMAND_DESCRIBE = "describe";
    public static final String COMMAND_DIFF = "diff";
    public static final String COMMAND_EDIT = "edit";
    public static final String COMMAND_INFO = "info";
    public static final String COMMAND_INTEGRATE = "integrate";
    public static final String COMMAND_FLUSH = "flush";
    public static final String COMMAND_FSTAT = "fstat";
    public static final String COMMAND_LABEL = "label";
    public static final String COMMAND_LABELS = "labels";
    public static final String COMMAND_LABELSYNC = "labelsync";
    public static final String COMMAND_LOGIN = "login";
    public static final String COMMAND_RESOLVE = "resolve";
    public static final String COMMAND_REVERT = "revert";
    public static final String COMMAND_SET = "set";
    public static final String COMMAND_SUBMIT = "submit";
    public static final String COMMAND_SYNC = "sync";
    public static final String COMMAND_USER = "user";
    public static final String COMMAND_WHERE = "where";
    public static final String FLAG_AFFECTED_CHANGELIST = "-e";
    public static final String FLAG_AUTO_MERGE = "-am";
    public static final String FLAG_CHANGELIST = "-c";
    public static final String FLAG_CLIENT = "-c";
    public static final String FLAG_DELETE = "-d";
    public static final String FLAG_DISPLAY_TICKET = "-p";
    public static final String FLAG_FILES_OPENED = "-W";
    public static final String FLAG_FORCE = "-f";
    public static final String FLAG_INPUT = "-i";
    public static final String FLAG_LABEL = "-l";
    public static final String FLAG_MAXIMUM = "-m";
    public static final String FLAG_OUTPUT = "-o";
    public static final String FLAG_PATH_IN_DEPOT_FORMAT = "-P";
    public static final String FLAG_PREVIEW = "-n";
    public static final String FLAG_RESTRICT_TO_CLIENT = "-Rc";
    public static final String FLAG_SHORT = "-s";
    public static final String FLAG_STATUS = "-s";
    public static final String FLAG_TYPE = "-t";
    public static final String FLAG_UNIFIED_DIFF = "-du";
    public static final String OPTION_LOCKED = "locked";
    public static final String VALUE_SUBMITTED = "submitted";
    public static final String VALUE_ALL_FILES = "...";

    public static final String FSTAT_CLIENT_FILE = "clientFile";
    public static final String FSTAT_DEPOT_FILE = "depotFile";
    public static final String FSTAT_PATH = "path";
    public static final String FSTAT_HEAD_ACTION = "headAction";
    public static final String FSTAT_HEAD_CHANGE = "headChange";
    public static final String FSTAT_HEAD_REVISION = "headRev";
    public static final String FSTAT_HEAD_TIME = "headTime";
    public static final String FSTAT_HEAD_REVISION_MOD_TIME = "headRevModTime";
    public static final String FSTAT_HEAD_TYPE = "headType";
    public static final String FSTAT_HAVE_REVISION = "haveRev";
    public static final String FSTAT_DESCRIPTION = "desc";
    public static final String FSTAT_DIGEST = "digest";
    public static final String FSTAT_FILE_SIZE = "fileSize";
    public static final String FSTAT_ACTION = "action";
    public static final String FSTAT_TYPE = "type";
    public static final String FSTAT_ACTION_OWNER = "actionOwner";
    public static final String FSTAT_CHANGE = "change";
    public static final String FSTAT_RESOLVED = "resolved";
    public static final String FSTAT_UNRESOLVED = "unresolved";
    public static final String FSTAT_OTHER_OPEN = "otherOpen";
    public static final String FSTAT_OTHER_LOCK = "otherLock";
    public static final String FSTATE_OTHER_ACTION = "otherAction";
    public static final String FSTATE_OUR_LOCK = "ourLock";

    public static final String ACTION_ADD = "add";
    public static final String ACTION_ADDED_AS = "added as";
    public static final String ACTION_BRANCH = "branch";
    public static final String ACTION_DELETE = "delete";
    public static final String ACTION_DELETED_AS = "deleted as";
    public static final String ACTION_EDIT = "edit";
    public static final String ACTION_INTEGRATE = "integrate";
    public static final String ACTION_MOVE_ADD = "move/add";
    public static final String ACTION_MOVE_DELETE = "move/delete";
    public static final String ACTION_REFRESHING = "refreshing";
    public static final String ACTION_UPDATING = "updating";
    

    public static final String REVISION_NONE = "none";

    // Artifact of p4 changes -s submitted -m 1:
    //   Change <number> on <date> by <user>@<client>
    public static final Pattern PATTERN_CHANGES = Pattern.compile("^Change ([0-9]+) on (.+) by (.+)@(.+) '(.*)'$", Pattern.MULTILINE);
    // User emails are part of a user spec.
    public static final Pattern PATTERN_EMAIL = Pattern.compile("Email:\\s*([^\\s]+@[^\\s]+)", Pattern.MULTILINE);

    public static final String[] EXECUTABLE_TYPES = {
            "xtext",
            "kxtext",
            "xbinary",
            "cxtext",
            "xltext",
            "uxbinary",
            "xtempobj",
            "xunicode"
    };

    public static final String RESOURCE_NAME = "p4";

    public static final String DEFAULT_P4 = "p4";
    public static final String P4_COMMAND = System.getProperty("pulse.p4.command", DEFAULT_P4);
    private static final String P4_COMMAND_PREFIX = "pulse.p4.command.";

    /**
     * Maps from a Perforce command (e.g. 'sync' to the binary that should be
     * executed for that command (it defaults to p4).
     *
     * @see #getP4Command
     */
    private static Map<String, String> commandMap = new HashMap<String, String>();
    static
    {
        Properties properties = System.getProperties();
        for(Map.Entry<Object,Object> property: properties.entrySet())
        {
            String key = (String) property.getKey();
            if(key.startsWith(P4_COMMAND_PREFIX))
            {
                commandMap.put(key.substring(P4_COMMAND_PREFIX.length()), (String) property.getValue());
            }
        }
    }

    public static final String SCRIPT_CHECKOUT = "p4.script.checkout";
    public static final String SCRIPT_UPDATE = "p4.script.update";
    
    public static final String PROPERTY_REVISION = "revision";
    public static final String PROPERTY_WORKSPACE = "workspace";

    private static Map<String, String> scriptMap = new HashMap<String, String>();
    static
    {
        scriptMap.put(SCRIPT_CHECKOUT, "p4 -c $(workspace) flush #none; p4 -c $(workspace) sync -f @$(revision)");
        scriptMap.put(SCRIPT_UPDATE, "p4 -c $(workspace) sync @$(revision)");
    }

    public static String getP4Command(String command)
    {
        String result = commandMap.get(command);
        if(result == null)
        {
            result = P4_COMMAND;
        }

        return result;
    }

    public static List<String[]> resolveScript(ExecutionContext context, String name)
    {
        String script = context.getString(name);
        if (script == null)
        {
            script = scriptMap.get(name);
        }

        List<String[]> resolved = new LinkedList<String[]>();
        String[] commands = StringUtils.split(script, ';', true);
        for (String command: commands)
        {
            command = command.trim();
            List<String> split = context.splitAndResolveVariables(command);
            resolved.add(split.toArray(new String[split.size()]));
        }

        return resolved;
    }
}
