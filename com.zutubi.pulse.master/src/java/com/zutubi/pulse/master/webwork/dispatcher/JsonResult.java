package com.zutubi.pulse.master.webwork.dispatcher;

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.webwork.dispatcher.WebWorkResultSupport;
import com.opensymphony.xwork.ActionInvocation;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.util.TextUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JsonResult extends WebWorkResultSupport
{
    private static final Logger LOG = Logger.getLogger(JsonResult.class);

    private JsonDefinitionLoader definitionLoader;

    protected void doExecute(String finalLocation, ActionInvocation ai) throws Exception
    {
        HttpServletResponse response = ServletActionContext.getResponse();
        OgnlValueStack stack = ai.getStack();

        
        Writer writer = new OutputStreamWriter(response.getOutputStream(), "UTF-8");
        JSONWriter jw = new JSONWriter(writer);

        // parse structural definition.
        if (!finalLocation.startsWith("/"))
        {
            finalLocation = ai.getProxy().getNamespace() + "/" + finalLocation;
        }
        Document doc = null;
        InputStream input = null;
        try
        {
            input = getDefinitionLoader().load(finalLocation);
            doc = parseInputStream(input);
        }
        finally
        {
            IOUtils.close(input);
        }

        Element root = doc.getDocumentElement();
        handleObject(root, jw, stack);

        response.setContentType("application/json"); // opera does not like this...

        writer.flush();
    }

    private Document parseInputStream(InputStream input) throws ParserConfigurationException, IOException, SAXException
    {
        // run some form of validation on the definition using a DTD to ensure that it is well formed.

        DocumentBuilder db;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        dbf.setNamespaceAware(true);

        db = dbf.newDocumentBuilder();
        db.setErrorHandler(new ErrorHandler()
        {
            public void warning(SAXParseException exception)
            {
            }

            public void error(SAXParseException exception) throws SAXException
            {
                LOG.error(exception.getMessage() + " at (" + exception.getLineNumber() + ":" + exception.getColumnNumber() + ")");
                throw exception;
            }

            public void fatalError(SAXParseException exception) throws SAXException
            {
                LOG.error(exception.getMessage() + " at (" + exception.getLineNumber() + ":" + exception.getColumnNumber() + ")");
                throw exception;
            }
        });
        return db.parse(input);
    }

    private void handleObject(Element e, JSONWriter jw, Object data) throws JSONException
    {
        List<Element> children = getChildElements(e);

        // everytime we see an object element, begin the object. root is an object.
        jw.object();

        for (Element elem : children)
        {
            handlePair(elem, jw, data);
        }

        jw.endObject();
    }

    private void handleArray(Element e, JSONWriter jw, Object data) throws JSONException
    {
        // take the data, extract the appropriate value and loop over the elements.
        String ref = e.getAttribute("ref");

        Object newData = data;
        if (TextUtils.stringSet(ref))
        {
            newData = evaluateReference(ref, data);
        }
        // expecting data to be a collection or array. If not, treat it as a single element in a list.

        Object[] array;
        if (newData == null)
        {
            array = new Object[0];
        }
        else if (newData instanceof Collection)
        {
            Collection<Object> c = (Collection<Object>) newData;
            array = c.toArray(new Object[c.size()]);
        }
        else if (newData.getClass().isArray())
        {
            array = (Object[]) newData;
        }
        else
        {
            array = new Object[]{newData};
        }

        jw.array();
        for (Object o : array)
        {
            handleValue(e, jw, o);
        }
        jw.endArray();
    }

    private void handleMap(Element e, JSONWriter jw, Object data) throws JSONException
    {
        String ref = e.getAttribute("ref");
        Object newData = evaluateReference(ref, data);

        if (!(newData instanceof Map))
        {
            return;
        }
        
        Map map = (Map) newData;
        jw.object();
        for (Object o : map.keySet())
        {
            jw.key(o.toString());
            handleValue(e, jw, map.get(o));
        }
        jw.endObject();
    }

    private void handlePair(Element e, JSONWriter jw, Object data) throws JSONException
    {
        // key.
        String key = e.getAttribute("key");
        jw.key(key);

        // value.
        handleValue(e, jw, data);
    }

    private void handleValue(Element e, JSONWriter jw, Object data) throws JSONException
    {
        List<Element> children = getChildElements(e);
        if (children.size() == 0)
        {
            String ref = e.getTextContent();
            if (TextUtils.stringSet(ref))
            {
                Object o = evaluateReference(ref, data);
                jw.value( (o != null) ? o : "");
            }
            else
            {
                jw.value(data);
            }
        }
        else
        {
            for (Element child : children)
            {
                if (child.getLocalName().equals("object"))
                {
                    handleObject(child, jw, data);
                }
                else if (child.getLocalName().equals("array"))
                {
                    handleArray(child, jw, data);
                }
                else if (child.getLocalName().equals("map"))
                {
                    handleMap(child, jw, data);
                }
                else // unknown.
                {
                    throw new JSONException("Unexpected element: " + child.getLocalName() + ". Expected 'object' or 'array'");
                }
            }
        }
    }

    private List<Element> getChildElements(Node n)
    {
        NodeList children = n.getChildNodes();
        List<Element> elements = new LinkedList<Element>();
        for (int i = 0; i < children.getLength(); i++)
        {
            Node child = children.item(i);
            if (child instanceof Element)
            {
                elements.add((Element)child);
            }
        }
        return elements;
    }

    private Object evaluateReference(String ref, Object data)
    {
        if (data instanceof OgnlValueStack)
        {
            OgnlValueStack stack = (OgnlValueStack) data;
            return stack.findValue(ref);
        }
        else
        {
            OgnlValueStack stack = new OgnlValueStack();
            stack.push(data);
            return stack.findValue(ref);
        }
    }

    public void setDefinitionLoader(JsonDefinitionLoader definitionLoader)
    {
        this.definitionLoader = definitionLoader;
    }

    public JsonDefinitionLoader getDefinitionLoader()
    {
        return this.definitionLoader;
    }

    public void setConfigurationManager(MasterConfigurationManager manager)
    {
        final MasterConfigurationManager config = manager;
        setDefinitionLoader(new JsonDefinitionLoader()
        {
            public InputStream load(String location) throws FileNotFoundException
            {
                File contentRoot = config.getSystemPaths().getContentRoot();
                File definition = new File(contentRoot, location);
                return new FileInputStream(definition);
            }
        });
    }
}
