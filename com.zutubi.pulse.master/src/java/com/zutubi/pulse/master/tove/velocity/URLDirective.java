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

package com.zutubi.pulse.master.tove.velocity;

import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.webwork.dispatcher.mapper.PulseActionMapper;
import com.zutubi.pulse.master.tove.webwork.ToveUtils;
import com.zutubi.util.StringUtils;
import com.zutubi.util.logging.Logger;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

/**
 * A directive to write out the path to an action in a consistent way.
 */
public class URLDirective extends ToveDirective
{
    private static final Logger LOG = Logger.getLogger(URLDirective.class);

    /**
     * The action to dispatch to.  Required.
     */
    private String action;

    /**
     * The path to pass to the action.  This field is optional.
     */
    private String path;

    private String namespace = PulseActionMapper.ADMIN_NAMESPACE;

    private MasterConfigurationManager configurationManager;

    /**
     * @see org.apache.velocity.runtime.directive.Directive#getName()
     */
    public String getName()
    {
        return "url";
    }

    /**
     * @see org.apache.velocity.runtime.directive.Directive#getType()
     * @see org.apache.velocity.runtime.directive.DirectiveConstants#LINE
     */
    public int getType()
    {
        return LINE;
    }

    public void setAction(String action)
    {
        this.action = action;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    /**
     * The namespace defines the url namespace that this form is being rendered in.  This is used by
     * the form generation process to determine the correct url to submit the form to.
     *
     * @param namespace in which this form is operating.
     */
    public void setNamespace(String namespace)
    {
        this.namespace = namespace;
    }

    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException
    {
        try
        {
            Map params = createPropertyMap(context, node);
            wireParams(params);

            writer.write(StringUtils.join("/", true, true, configurationManager.getSystemConfig().getContextPath(), ToveUtils.getConfigURL(path, action, null, namespace)));

            return true;
        }
        catch (Exception e)
        {
            writer.write(renderError("Failed to render. Unexpected " + e.getClass() + ": " + e.getMessage()));
            LOG.severe(e);
            return true;
        }
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
