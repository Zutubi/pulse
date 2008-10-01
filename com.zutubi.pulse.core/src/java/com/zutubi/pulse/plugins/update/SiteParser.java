package com.zutubi.pulse.plugins.update;

import static com.zutubi.pulse.plugins.update.UpdateParserUtils.*;
import com.zutubi.pulse.core.util.XMLUtils;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Reads in a Eclipse Update site.xml file and spits out a model.
 */
public class SiteParser
{
    private static final String ELEMENT_ARCHIVE = "archive";
    private static final String ELEMENT_CATEGORY = "category";
    private static final String ELEMENT_CATEGORY_DEF = "category-def";
    private static final String ELEMENT_FEATURE = "feature";
    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_LABEL = "label";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_PATH = "path";
    private static final String ATTRIBUTE_VERSION = "version";

    public Site parse(URL url) throws IOException, ParsingException
    {
        return parse(url, url.openStream());
    }

    private Site parse(URL url, InputStream in) throws IOException, ParsingException
    {
        Document doc = XMLUtils.streamToDoc(in);
        Element root = doc.getRootElement();
        String urlString = root.getAttributeValue(ATTRIBUTE_URL);
        if (urlString != null)
        {
            url = resolveURL(urlString, url);
        }

        Site site = new Site(url, getOptionalText(root, ELEMENT_DESCRIPTION));

        Elements archives = root.getChildElements(ELEMENT_ARCHIVE);
        for (int i = 0; i < archives.size(); i++)
        {
            processArchive(site, archives.get(i));
        }

        Elements categories = root.getChildElements(ELEMENT_CATEGORY_DEF);
        for (int i = 0; i < categories.size(); i++)
        {
            processCategory(site, categories.get(i));
        }

        Elements features = root.getChildElements(ELEMENT_FEATURE);
        for (int i = 0; i < features.size(); i++)
        {
            processFeature(site, features.get(i));
        }

        return site;
    }

    private void processArchive(Site site, Element element) throws ParsingException
    {
        String path = XMLUtils.getRequiredAttribute(element, ATTRIBUTE_PATH);
        URL url = getRequiredURL(site, element);
        site.addArchive(path, url);
    }

    private void processCategory(Site site, Element element) throws ParsingException
    {
        String name = XMLUtils.getRequiredAttribute(element, ATTRIBUTE_NAME);
        String label = XMLUtils.getRequiredAttribute(element, ATTRIBUTE_LABEL);

        Category category = new Category(name, label, getOptionalText(element, ELEMENT_DESCRIPTION));
        site.addCategory(category);
    }

    private void processFeature(Site site, Element element) throws ParsingException
    {
        String id = XMLUtils.getRequiredAttribute(element, ATTRIBUTE_ID);
        String version = XMLUtils.getRequiredAttribute(element, ATTRIBUTE_VERSION);
        URL url = getRequiredURL(site, element);

        FeatureReference featureReference = new FeatureReference(id, version, url);
        site.addFeatureReference(featureReference);

        Elements categories = element.getChildElements(ELEMENT_CATEGORY);
        for(int i = 0; i < categories.size(); i++)
        {
            String name = XMLUtils.getRequiredAttribute(categories.get(i), ATTRIBUTE_NAME);
            Category category = site.getCategory(name);
            if(category == null)
            {
                throw new ParsingException("FeatureManifest '" + id + "' refers to unknown category '" + name + "'");
            }

            category.addFeatureReference(featureReference);
        }
    }

    public static void main(String[] argv)
    {
        SiteParser parser = new SiteParser();
        try
        {
            Site s = parser.parse(new URL(argv[0]));
            System.out.println(s);
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
