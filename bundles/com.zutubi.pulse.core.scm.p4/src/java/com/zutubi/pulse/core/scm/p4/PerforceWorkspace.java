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
import com.zutubi.util.StringUtils;
import com.zutubi.util.io.IOUtils;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a Perforce client workspace, used for talking to the Perforce
 * server.  Workspaces are obtained from the {@link PerforceWorkspaceManager}.
 */
public class PerforceWorkspace
{
    private static final Pattern PATTERN_TAG = Pattern.compile("^(\\w+):(.*)");

    // The tags we actually interpret.
    private static final String TAG_CLIENT = "Client";
    private static final String TAG_ACESS = "Access";
    private static final String TAG_HOST = "Host";
    private static final String TAG_DESCRIPTION = "Description";
    private static final String TAG_ROOT = "Root";
    private static final String TAG_OPTIONS = "Options";
    private static final String TAG_STREAM = "Stream";
    private static final String TAG_VIEW = "View";

    private static final String COMMENT_MARKER = "#";
    private static final String SEPARATOR = ":";

    private String name;
    private String access;
    private String host;
    private List<String> description;
    private String root;
    private Set<String> options = new HashSet<String>();
    private List<String> view;
    private String stream;
    private Map<String, List<String>> unrecognised = new HashMap<String, List<String>>();

    private boolean temporary;

    /**
     * Creates a new workspace with required details.
     *
     * @param name name of the workspace
     * @param root client root path
     * @param view view of the depot, as a list of individual mappings (each
     *        element corresponds to a line in the View: part of a
     *        specification).
     */
    public PerforceWorkspace(String name, String root, List<String> view)
    {
        this.name = name;
        this.root = root;
        this.view = new LinkedList<String>(view);
    }

    private PerforceWorkspace(Map<String, List<String>> taggedValues) throws ScmException
    {
        name = extractRequiredSingleValue(taggedValues, TAG_CLIENT);
        root = extractRequiredSingleValue(taggedValues, TAG_ROOT);
        view = extractRequiredFieldValue(taggedValues, TAG_VIEW);

        List<String> value = taggedValues.remove(TAG_ACESS);
        if (value != null && value.size() == 1)
        {
            access = value.get(0);
        }

        value = taggedValues.remove(TAG_HOST);
        if (value != null)
        {
            host = StringUtils.join("", value);
        }

        value = taggedValues.remove(TAG_DESCRIPTION);
        if (value != null)
        {
            description = new LinkedList<String>(value);
        }

        value = taggedValues.remove(TAG_OPTIONS);
        if (value != null)
        {
            options = new HashSet<String>(Arrays.asList(StringUtils.join(" ", value).split("\\s+")));
        }

        value = taggedValues.remove(TAG_STREAM);
        if (value != null)
        {
            stream = StringUtils.join("", value);
        }

        unrecognised = new HashMap<String, List<String>>(taggedValues);
    }

    private String extractRequiredSingleValue(Map<String, List<String>> taggedValues, String tag) throws ScmException
    {
        List<String> value = extractRequiredFieldValue(taggedValues, tag);
        if (value.size() != 1)
        {
            throw new ScmException("Expected a single value for field '" + tag + "', got '" + value.toString() + "'");
        }

        return value.get(0);
    }

    private List<String> extractRequiredFieldValue(Map<String, List<String>> taggedValues, String tag) throws ScmException
    {
        List<String> value = taggedValues.remove(tag);
        if (value == null)
        {
            throw new ScmException("Required field '" + tag + "' not specified");
        }

        return value;
    }

    /**
     * Creates a workspace by parsing a specification, for example what is given by
     * "p4 client -o".
     * 
     * @param specification the specification to parse
     * @return workspace corresponding to the specification
     * @throws ScmException if the specification cannot be parsed, or does not
     *         include all required fields
     */
    public static PerforceWorkspace parseSpecification(String specification) throws ScmException
    {
        Map<String, List<String>> taggedValues = new HashMap<String, List<String>>();
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new StringReader(specification));
            String line;
            String currentTag = null;
            List<String> currentValue = new LinkedList<String>();
            while ((line = reader.readLine()) != null)
            {
                if (line.startsWith(COMMENT_MARKER))
                {
                    continue;
                }

                String tag = getTag(line);
                if (tag == null)
                {
                    if (currentTag != null)
                    {
                        line = line.trim();
                        if (line.length() > 0)
                        {
                            currentValue.add(line);
                        }
                    }
                }
                else
                {
                    if (currentTag != null && currentValue.size() > 0)
                    {
                        taggedValues.put(currentTag, currentValue);
                        currentValue = new LinkedList<String>();
                    }

                    currentTag = tag;
                    String value = getTaggedValue(line).trim();
                    if (value.length() > 0)
                    {
                        currentValue.add(value);
                    }
                }
            }

            if (currentTag != null && currentValue.size() > 0)
            {
                taggedValues.put(currentTag, currentValue);
            }
        }
        catch (IOException e)
        {
            // Reading from a string this never happens, but just in case we
            // propagate.
            throw new ScmException(e);
        }
        finally
        {
            IOUtils.close(reader);
        }

        return new PerforceWorkspace(taggedValues);
    }

    private static String getTag(String line)
    {
        Matcher matcher = PATTERN_TAG.matcher(line);
        if (matcher.matches())
        {
            return matcher.group(1);
        }
        else
        {
            return null;
        }
    }

    private static String getTaggedValue(String line) throws ScmException
    {
        Matcher matcher = PATTERN_TAG.matcher(line);
        if (!matcher.matches())
        {
            throw new ScmException("Internal error: trying to extract value from line '" + line + "'");
        }

        return matcher.group(2);
    }

    /**
     * Serialises this workspace to a specification suitable for passing to
     * Perforce (e.g. as input to "p4 client -i").
     *
     * @return the serialised form of this workspace
     */
    public String toSpecification()
    {
        StringWriter stringWriter = new StringWriter(512);
        PrintWriter writer = new PrintWriter(stringWriter);

        writeSingleValue(writer, TAG_CLIENT, name);
        // We don't write out the access field, it is managed by the server
        writeSingleValue(writer, TAG_HOST, host);
        writeMultiValue(writer, TAG_DESCRIPTION, description);
        writeSingleValue(writer, TAG_ROOT, root);
        writeSingleValue(writer, TAG_OPTIONS, StringUtils.join(" ", options));
        if (StringUtils.stringSet(stream))
        {
            writeSingleValue(writer, TAG_STREAM, stream);
        }
        writeMultiValue(writer, TAG_VIEW, view);

        for (Map.Entry<String, List<String>> entry: unrecognised.entrySet())
        {
            List<String> value = entry.getValue();
            if (value.size() == 1)
            {
                writeSingleValue(writer, entry.getKey(), value.get(0));
            }
            else
            {
                writeMultiValue(writer, entry.getKey(), value);
            }
        }

        return stringWriter.toString();
    }

    private void writeSingleValue(PrintWriter writer, String tag, String value)
    {
        writer.print(tag);
        writer.print(SEPARATOR);
        if (value != null)
        {
            writer.print("\t");
            writer.println(value);
        }
        writer.println();
    }

    private void writeMultiValue(PrintWriter writer, String tag, List<String> value)
    {
        if (value != null)
        {
            writer.print(tag);
            writer.println(SEPARATOR);
            for (String line: value)
            {
                writer.print("\t");
                writer.println(line);
            }

            writer.println();
        }
    }

    /**
     * Renames this workspace, which both updates the name and fixes references
     * to the name in the view.
     *
     * @param newName the new name for this workspace
     */
    public void rename(String newName)
    {
        List<String> newView = new LinkedList<String>();
        for (String mapping: view)
        {
            newView.add(mapping.replaceAll("//" + Pattern.quote(name) + "/", Matcher.quoteReplacement("//" + newName + "/")));
        }

        name = newName;
        view = newView;
    }

    /**
     * @return the workspace name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return the last access time for the client (may be null for a new
     *         client)
     */
    public String getAccess()
    {
        return access;
    }

    /**
     * @return host the workspace is tied to (may be null)
     */
    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    /**
     * @return workspace description (for human consumption)
     */
    public List<String> getDescription()
    {
        return Collections.unmodifiableList(description);
    }

    public void setDescription(List<String> description)
    {
        this.description = description;
    }

    /**
     * @return the absolute path of the workspace root
     */
    public String getRoot()
    {
        return root;
    }

    public void setRoot(String root)
    {
        this.root = root;
    }

    /**
     * @return the set of options enabled for this workspace
     */
    public Set<String> getOptions()
    {
        return Collections.unmodifiableSet(options);
    }

    public void setOptions(List<String> options)
    {
        this.options = new HashSet<String>(options);
    }

    /**
     * Adds the given option.
     *
     * @param name option name, e.g. "locked"
     */
    public void addOption(String name)
    {
        options.add(name);
    }

    /**
     * Removes the given option.
     *
     * @param name option name, e.g. "locked"
     */
    public void deleteOption(String name)
    {
        options.remove(name);
    }

    /**
     * @return the workspaces view of the depot, as a list of lines as they
     *         would appear in the View: field of a specification
     */
    public List<String> getView()
    {
        return Collections.unmodifiableList(view);
    }

    public void setView(List<String> view)
    {
        this.view = view;
    }

    /**
     * @return the stream to associate with the workspace (Perforce 2011.1 and later)
     */
    public String getStream()
    {
        return stream;
    }

    public void setStream(String stream)
    {
        this.stream = stream;
    }

    /**
     * @return true if this workspace should be deleted when the operation is
     *         completed, false if it should persist (used by the
     *         {@link PerforceWorkspaceManager}.
     */
    public boolean isTemporary()
    {
        return temporary;
    }

    public void setTemporary(boolean temporary)
    {
        this.temporary = temporary;
    }

    /**
     * @return a mapping of all unrecognised fields parsed from a specification
     *         (retained so they can be preserved when serialising)
     */
    public Map<String, List<String>> getUnrecognised()
    {
        return Collections.unmodifiableMap(unrecognised);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        PerforceWorkspace workspace = (PerforceWorkspace) o;

        if (temporary != workspace.temporary)
        {
            return false;
        }
        if (description != null ? !description.equals(workspace.description) : workspace.description != null)
        {
            return false;
        }
        if (host != null ? !host.equals(workspace.host) : workspace.host != null)
        {
            return false;
        }
        if (view != null ? !view.equals(workspace.view) : workspace.view != null)
        {
            return false;
        }
        if (stream != null ? !stream.equals(workspace.stream) : workspace.stream != null)
        {
            return false;
        }
        if (!name.equals(workspace.name))
        {
            return false;
        }
        if (!options.equals(workspace.options))
        {
            return false;
        }
        if (!root.equals(workspace.root))
        {
            return false;
        }

        return unrecognised.equals(workspace.unrecognised);
    }

    @Override
    public int hashCode()
    {
        int result = name.hashCode();
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (view != null ? view.hashCode() : 0);
        result = 31 * result + (stream != null ? stream.hashCode() : 0);
        result = 31 * result + root.hashCode();
        result = 31 * result + options.hashCode();
        result = 31 * result + unrecognised.hashCode();
        result = 31 * result + (temporary ? 1 : 0);
        return result;
    }
}
