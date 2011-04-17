package com.zutubi.pulse.core.scm.hg;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;

import com.zutubi.pulse.core.scm.api.Changelist;
import com.zutubi.pulse.core.scm.api.FileChange;
import com.zutubi.pulse.core.scm.api.Revision;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.api.FileChange.Action;
import com.zutubi.pulse.core.util.api.XMLUtils;

/**
 * Parses XML output from hg log --style xml, extracting changeset entries.
 * The output format is:
 *
 * <pre>{@code <log>
 *     <logentry revision="123" node="c4b2d21db51a531b069dd7f0d7fc10996c9c70c8">
 *         <tag>tip</tag>
 *         <author email="joeblogs@example.com">joeblogs</author>
 *         <date>2009-06-08T17:31:04.848536Z</date>
 *         <paths>
 *             <path action="M">/path/to/file/from/repository/root</path>
 *             <!-- ... -->
 *         </paths>
 *         <msg xml:space="preserve">Commit message here</msg>
 *     </logentry>
 *     <!-- ... -->
 * </log>}</pre>
 */
public class LogParser
{
    private static final String ELEMENT_AUTHOR = "author";
    private static final String ELEMENT_DATE = "date";
    private static final String ELEMENT_LOG_ENTRY = "logentry";
    private static final String ELEMENT_MESSAGE = "msg";
    private static final String ELEMENT_PATH = "path";
    private static final String ELEMENT_PATHS = "paths";

    private static final String ATTRIBUTE_ACTION = "action";
    private static final String ATTRIBUTE_NODE = "node";

    /**
     * Parses XML-formatted output from hg log into corresponding changelists.
     *
     * @param xml the raw XML output from hg log --style xml
     * @return a list of changelists converted from log entries in the XML
     * @throws ScmException on any error
     */
    public static List<Changelist> parse(String xml) throws ScmException
    {
        List<Changelist> result = new LinkedList<Changelist>();
        try
        {
            Document doc = XMLUtils.streamToDoc(new ByteArrayInputStream(xml.getBytes()));
            processDocument(doc, result);
            return result;
        }
        catch (ParsingException e)
        {
            throw new ScmException("Unable to parse log output: " + e.getMessage(), e);
        }
        catch (IOException e)
        {
            throw new ScmException("I/O error parsing log output: " + e.getMessage(), e);
        }
    }

    private static void processDocument(Document doc, List<Changelist> result) throws ParsingException
    {
        Element root = doc.getRootElement();
        Elements entryElements = root.getChildElements(ELEMENT_LOG_ENTRY);
        for (int i = 0; i < entryElements.size(); i++)
        {
            processEntry(entryElements.get(i), result);
        }
    }

    private static void processEntry(Element element, List<Changelist> result) throws ParsingException
    {
        Revision revision = new Revision(XMLUtils.getRequiredAttributeValue(element, ATTRIBUTE_NODE));
        Element authorElement = element.getFirstChildElement(ELEMENT_AUTHOR);
        String author = authorElement == null ? "anonymous" : XMLUtils.getText(authorElement, "?");
        String dateString = XMLUtils.getRequiredChildText(element, ELEMENT_DATE, true);
        String message = XMLUtils.getChildText(element, ELEMENT_MESSAGE, "");
        List<FileChange> changes = processChanges(element.getFirstChildElement(ELEMENT_PATHS), revision);

        result.add(new Changelist(revision, parseDate(dateString), author, message, changes));
    }

    private static List<FileChange> processChanges(Element pathsElement, Revision revision)
    {
        if (pathsElement == null)
        {
            return Collections.emptyList();
        }
        else
        {
            List<FileChange> result = new LinkedList<FileChange>();
            Elements elements = pathsElement.getChildElements(ELEMENT_PATH);
            for (int i = 0; i < elements.size(); i++)
            {
                Element element = elements.get(i);
                result.add(new FileChange(XMLUtils.getText(element), revision, convertAction(XMLUtils.getAttributeValue(element, ATTRIBUTE_ACTION, "?"))));
            }

            return result;
        }
    }

    private static Action convertAction(String actionString)
    {
        if (actionString.length() != 1)
        {
            return Action.UNKNOWN;
        }

        switch (actionString.charAt(0))
        {
            case'A':
                return FileChange.Action.ADD;
            case'D':
                return FileChange.Action.DELETE;
            case'M':
                return FileChange.Action.EDIT;
            case'R':
                return FileChange.Action.MOVE;
            default:
                return FileChange.Action.UNKNOWN;
        }
    }

    private static long parseDate(String dateString) throws ParsingException
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        try
        {
            return dateFormat.parse(dateString.replaceAll("(\\d\\d):(\\d\\d)$", "$1$2")).getTime();
        }
        catch (ParseException e)
        {
            throw new ParsingException("Unparseable date '" + dateString + "'");
        }
    }
}
