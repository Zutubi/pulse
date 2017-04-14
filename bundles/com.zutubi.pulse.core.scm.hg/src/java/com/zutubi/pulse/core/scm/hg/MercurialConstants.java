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

package com.zutubi.pulse.core.scm.hg;

import com.zutubi.util.SystemUtils;

/**
 * Shared constants used by the hg implementation.
 */
public class MercurialConstants
{
    /**
     * Pulse system property used to override the default hg command line executable.
     */
    public static final String PROPERTY_HG_COMMAND = "pulse.hg.command";

    /**
     * The default hg executable, usually "hg" but "hg.exe" on a windows
     * system.  May be overridden with a system property.
     */
    public static final String DEFAULT_HG = SystemUtils.IS_WINDOWS ? "hg.exe" : "hg";

    public static final String RESOURCE_NAME = "mercurial";
    
    public static final String COMMAND_ADD = "add";
    public static final String COMMAND_BRANCH = "branch";
    public static final String COMMAND_CAT = "cat";
    public static final String COMMAND_CLONE = "clone";
    public static final String COMMAND_DIFF = "diff";
    public static final String COMMAND_IMPORT = "import";
    public static final String COMMAND_INCOMING = "incoming";
    public static final String COMMAND_LOG = "log";
    public static final String COMMAND_PARENTS = "parents";
    public static final String COMMAND_PULL = "pull";
    public static final String COMMAND_PUSH = "push";
    public static final String COMMAND_REMOVE = "remove";
    public static final String COMMAND_TAG = "tag";
    public static final String COMMAND_TAGS = "tags";
    public static final String COMMAND_UPDATE = "update";

    public static final String FLAG_BRANCH = "--branch";
    public static final String FLAG_CHANGE = "--change";
    public static final String FLAG_FORCE = "--force";
    public static final String FLAG_GIT_FORMAT = "--git";
    public static final String FLAG_LIMIT = "--limit";
    public static final String FLAG_MESSAGE = "--message";
    public static final String FLAG_NO_COMMIT = "--no-commit";
    public static final String FLAG_NO_UPDATE = "--noupdate";
    public static final String FLAG_NEWEST_FIRST = "--newest-first";
    public static final String FLAG_QUIET = "--quiet";
    public static final String FLAG_REVISION = "--rev";
    public static final String FLAG_STYLE = "--style";
    public static final String FLAG_TEMPLATE = "--template";
    public static final String FLAG_VERBOSE = "--verbose";

    public static final String BRANCH_DEFAULT = "default";

    public static final String REVISION_NULL = "null";
    public static final String REVISION_ZERO = "0";
    public static final String REVISION_TIP = "tip";

    public static final String STYLE_XML = "xml";
    public static final String TEMPLATE_NODE = "{node}";
}
