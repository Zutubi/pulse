package com.zutubi.plugins.internal;

import com.zutubi.plugins.*;
import com.zutubi.plugins.PluginException;
import com.zutubi.plugins.PluginParseException;
import nu.xom.*;

import java.io.IOException;
import java.io.InputStream;

/**
 * <class-comment/>
 */
public class XMLPluginDescriptorSupport
{
    private ComponentDescriptorFactory descriptorFactory;

    public void setDescriptorFactory(ComponentDescriptorFactory descriptorFactory)
    {
        this.descriptorFactory = descriptorFactory;
    }

    /**
     * The Load Plugin method takes the xml plugin description from the input stream and uses it to configure
     * the configurable plugin instance.
     *
     * @param pluginXml is an input stream for an xml document that contains a plugin definition.
     * @param plugin instance to which the contents of the provided xml document will be applied.
     *
     * @return the plugin instance for convenience.
     *
     * @throws com.zutubi.plugins.PluginException
     *
     * @throws java.io.IOException is thrown if there is a problem reading data from the input stream.
     */
    public DefaultPlugin loadPlugin(InputStream pluginXml, DefaultPlugin plugin) throws PluginException, IOException
    {
        Document doc = createDocument(pluginXml);
        Element root = doc.getRootElement();

        readPlugin(root, plugin);

        Element info = root.getFirstChildElement("plugin-info");
        if (info != null)
        {
            readPluginInfo(info, plugin);
        }

        // read the component descriptors.
        readComponentDescriptors(root, plugin);
        return plugin;
    }

    private void readPlugin(Element root, DefaultPlugin plugin)
    {
        plugin.setName(root.getAttributeValue("name"));
        plugin.setKey(root.getAttributeValue("key"));
    }

    private void readComponentDescriptors(Element root, DefaultPlugin plugin)
    {
        Elements children = root.getChildElements();
        for (int i = 0; i < children.size(); i++)
        {
            Element child = children.get(i);

            String name = child.getLocalName();
            if (descriptorFactory.supportsComponentDescriptor(name))
            {
                ComponentDescriptor descriptorInstance = descriptorFactory.createComponentDescriptor(name, child);
                descriptorInstance.setPlugin(plugin);
                plugin.addComponentDescriptor(descriptorInstance);
            }
        }
    }

    private void readPluginInfo(Element info, DefaultPlugin plugin)
    {
        PluginInformation pluginInfo = new PluginInformation();
        Element infoDesc = info.getFirstChildElement("description");
        if (infoDesc != null)
        {
            pluginInfo.setDescription(infoDesc.getValue());
        }

        Element infoVersion = info.getFirstChildElement("version");
        if (infoVersion != null)
        {
            pluginInfo.setPluginVersion(infoVersion.getValue());
        }
        Element infoAppVersion = info.getFirstChildElement("application-version");
        if (infoAppVersion != null)
        {
            pluginInfo.setMinSupportedAppVersion(infoAppVersion.getAttributeValue("min"));
            pluginInfo.setMaxSupportedAppVersion(infoAppVersion.getAttributeValue("max"));
        }
        Element infoVendor = info.getFirstChildElement("vendor");
        if (infoVendor != null)
        {
            pluginInfo.setVendorName(infoVendor.getAttributeValue("name"));
            pluginInfo.setVendorURL(infoVendor.getAttributeValue("url"));
        }

        plugin.setInfo(pluginInfo);
    }

    private Document createDocument(InputStream pluginXml) throws PluginException, IOException
    {
        Builder builder = new Builder();

        Document doc;
        try
        {
            doc = builder.build(pluginXml);
        }
        catch (ParsingException pex)
        {
            throw new PluginParseException(pex);
        }
        return doc;
    }


}
