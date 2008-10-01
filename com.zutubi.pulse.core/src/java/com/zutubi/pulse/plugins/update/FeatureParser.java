package com.zutubi.pulse.plugins.update;

import static com.zutubi.pulse.plugins.update.UpdateParserUtils.*;
import com.zutubi.pulse.plugins.PluginVersion;
import com.zutubi.pulse.core.util.XMLUtils;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 */
public class FeatureParser
{
    private static final String ATTRIBUTE_DOWNLOAD_SIZE = "download-size";
    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_INSTALL_SIZE = "install-size";
    private static final String ATTRIBUTE_FEATURE = "feature";
    private static final String ATTRIBUTE_IMAGE = "image";
    private static final String ATTRIBUTE_LABEL = "label";
    private static final String ATTRIBUTE_MATCH = "match";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_OPTIONAL = "optional";
    private static final String ATTRIBUTE_PLUGIN = "plugin";
    private static final String ATTRIBUTE_PROVIDER_NAME = "provider-name";
    private static final String ATTRIBUTE_TYPE = "type";
    private static final String ATTRIBUTE_UNPACK = "unpack";
    private static final String ATTRIBUTE_VERSION = "version";

    private static final String ELEMENT_COPYRIGHT = "copyright";
    private static final String ELEMENT_DISCOVERY = "discovery";
    private static final String ELEMENT_IMPORT = "import";
    private static final String ELEMENT_INCLUDES = "includes";
    private static final String ELEMENT_LICENSE = "license";
    private static final String ELEMENT_PLUGIN = "plugin";
    private static final String ELEMENT_REQUIRES = "requires";
    private static final String ELEMENT_UPDATE = "update";
    private static final String ELEMENT_URL = "url";

    public FeatureManifest parse(URL url, InputStream in) throws IOException, ParsingException
    {
        Document doc = XMLUtils.streamToDoc(in);
        Element root = doc.getRootElement();
        String id = XMLUtils.getRequiredAttribute(root, ATTRIBUTE_ID);
        PluginVersion version = getVersion(root, true);
        String label = XMLUtils.getAttributeDefault(root, ATTRIBUTE_LABEL, id);
        String description = getOptionalText(root, ELEMENT_DESCRIPTION);
        String providerName = root.getAttributeValue(ATTRIBUTE_PROVIDER_NAME);
        String image = root.getAttributeValue(ATTRIBUTE_IMAGE);

        FeatureManifest featureManifest = new FeatureManifest(id, version, label, description, providerName, UpdateParserUtils.resolveURL(image, url));
        featureManifest.setCopyright(getOptionalText(root, ELEMENT_COPYRIGHT));
        featureManifest.setLicense(getOptionalText(root, ELEMENT_LICENSE));

        Elements children = root.getChildElements(ELEMENT_URL);
        if(children.size() > 0)
        {
            processURL(featureManifest, children.get(0));
        }

        children = root.getChildElements(ELEMENT_INCLUDES);
        for(int i = 0; i < children.size(); i++)
        {
            processInclude(featureManifest, children.get(i));
        }

        children = root.getChildElements(ELEMENT_REQUIRES);
        if(children.size() > 0)
        {
            children = children.get(0).getChildElements(ELEMENT_IMPORT);
            for(int i = 0; i < children.size(); i++)
            {
                processImport(featureManifest, children.get(i));
            }
        }

        children = root.getChildElements(ELEMENT_PLUGIN);
        for(int i = 0; i < children.size(); i++)
        {
            processPlugin(featureManifest, children.get(i));
        }

        return featureManifest;
    }

    private void processURL(FeatureManifest featureManifest, Element element) throws ParsingException
    {
        Elements children = element.getChildElements(ELEMENT_UPDATE);
        if(children.size() > 0)
        {
            Element e = children.get(0);
            URL url = getRequiredURL(null, e);
            String label = XMLUtils.getAttributeDefault(e, ATTRIBUTE_LABEL, url.getHost());
            featureManifest.setUpdateSite(new UpdateSiteReference(url, label));
        }

        children = element.getChildElements(ELEMENT_DISCOVERY);
        for(int i = 0; i < children.size(); i++)
        {
            Element e = children.get(i);
            URL url = getRequiredURL(null, e);
            String label = XMLUtils.getAttributeDefault(e, ATTRIBUTE_LABEL, url.getHost());
            boolean web = "web".equals(e.getAttributeValue(ATTRIBUTE_TYPE));
            featureManifest.addDiscoverySite(new DiscoverySiteReference(url, label, web));
        }
    }

    private void processInclude(FeatureManifest featureManifest, Element element) throws ParsingException
    {
        String id = XMLUtils.getRequiredAttribute(element, ATTRIBUTE_ID);
        String version = XMLUtils.getRequiredAttribute(element, ATTRIBUTE_VERSION);
        String name = XMLUtils.getAttributeDefault(element, ATTRIBUTE_NAME, id);
        boolean optional = Boolean.valueOf(element.getAttributeValue(ATTRIBUTE_OPTIONAL));

        featureManifest.addInclusion(new FeatureInclusion(id, version, name, optional));
    }

    private void processImport(FeatureManifest featureManifest, Element element) throws ParsingException
    {
        String id = element.getAttributeValue(ATTRIBUTE_PLUGIN);
        boolean isFeature = false;
        if(id == null)
        {
            isFeature = true;
            id = element.getAttributeValue(ATTRIBUTE_FEATURE);
            if(id == null)
            {
                throw new ParsingException("Element '" + ELEMENT_IMPORT + "' requires one of either '" + ATTRIBUTE_FEATURE + "' or '" + ATTRIBUTE_PLUGIN + "'");
            }
        }

        PluginVersion version = getVersion(element, false);
        VersionMatch match = VersionMatch.COMPATIBLE;
        String matchString = element.getAttributeValue(ATTRIBUTE_MATCH);

        if(matchString != null)
        {
            try
            {
                match = VersionMatch.valueOf(matchString.toUpperCase());
            }
            catch (IllegalArgumentException e)
            {
                throw new ParsingException("Invalid match value '" + matchString + "'");
            }
        }

        featureManifest.addRequirement(new FeatureRequirement(isFeature, id, version, match));
    }

    private void processPlugin(FeatureManifest featureManifest, Element element) throws ParsingException
    {
        String id = XMLUtils.getRequiredAttribute(element, ATTRIBUTE_ID);
        String version = XMLUtils.getRequiredAttribute(element, ATTRIBUTE_VERSION);
        int downloadSize = getSize(element, ATTRIBUTE_DOWNLOAD_SIZE);
        int installSize = getSize(element, ATTRIBUTE_INSTALL_SIZE);
        boolean unpack = Boolean.valueOf(element.getAttributeValue(ATTRIBUTE_UNPACK));

        featureManifest.addPlugin(new PluginReference(id, version, downloadSize, installSize, unpack));
    }

    private int getSize(Element element, String attribute) throws ParsingException
    {
        int size = -1;
        String sizeString = element.getAttributeValue(attribute);
        if(sizeString != null)
        {
            try
            {
                size = Integer.parseInt(sizeString);
            }
            catch (NumberFormatException e)
            {
                throw new ParsingException("Invalid size '" + sizeString + "' specified");
            }
        }

        return size;
    }

    public static void main(String[] argv)
    {
        FeatureParser parser = new FeatureParser();
        try
        {
            URL url = new URL(argv[0]);
            FeatureManifest f = parser.parse(new URL("http://dummy/feature.xml"), url.openStream());
            System.out.println(f);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (ParsingException e)
        {
            e.printStackTrace();
        }
    }
}
