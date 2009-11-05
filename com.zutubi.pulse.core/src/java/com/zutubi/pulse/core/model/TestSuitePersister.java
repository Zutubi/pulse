package com.zutubi.pulse.core.model;

import com.zutubi.pulse.core.postprocessors.api.TestResult;
import com.zutubi.pulse.core.postprocessors.api.TestStatus;
import com.zutubi.pulse.core.util.api.XMLUtils;
import com.zutubi.util.FileSystemUtils;
import com.zutubi.util.WebUtils;
import nu.xom.*;
import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 */
public class TestSuitePersister
{
    public static final String SUITE_FILE_NAME = "suite.xml";
    public static final String ELEMENT_SUITE = "suite";
    public static final String ELEMENT_CASE = "case";
    public static final String ELEMENT_MESSAGE = "message";
    public static final String ENCODED_PREFIX = "encoded-";
    public static final String ATTRIBUTE_TOTAL = "total";
    public static final String ATTRIBUTE_EXPECTED_FAILURES = "expected-failures";
    public static final String ATTRIBUTE_FAILURES = "failures";
    public static final String ATTRIBUTE_ERRORS = "errors";
    public static final String ATTRIBUTE_SKIPPED = "skipped";
    public static final String ATTRIBUTE_NAME = "name";
    public static final String ATTRIBUTE_DURATION = "duration";
    public static final String ATTRIBUTE_STATUS = "status";
    public static final String ATTRIBUTE_BROKEN_SINCE = "broken-since";
    public static final String ATTRIBUTE_BROKEN_NUMBER = "broken-number";
    public static final String ATTRIBUTE_FIXED = "fixed";


    public void write(PersistentTestSuiteResult suite, File directory) throws IOException
    {
        for (PersistentTestSuiteResult childSuite : suite.getSuites())
        {
            File suiteDir = new File(directory, WebUtils.formUrlEncode(childSuite.getName()));
            FileSystemUtils.createDirectory(suiteDir);

            write(childSuite, suiteDir);
        }

        writeCases(suite, new File(directory, SUITE_FILE_NAME));
    }

    private void writeCases(PersistentTestSuiteResult suite, File file) throws IOException
    {
        Document doc = suiteToDoc(suite);
        XMLUtils.writeDocument(file, doc);
    }

    private Document suiteToDoc(PersistentTestSuiteResult suite)
    {
        Element root = new Element(ELEMENT_SUITE);
        root.addAttribute(new Attribute(ATTRIBUTE_DURATION, Long.toString(suite.getDuration())));

        for (PersistentTestSuiteResult child : suite.getSuites())
        {
            Element suiteElement = new Element(ELEMENT_SUITE);
            suiteElement.addAttribute(safeAttribute(ATTRIBUTE_NAME, child.getName()));
            suiteElement.addAttribute(new Attribute(ATTRIBUTE_TOTAL, Integer.toString(child.getTotal())));
            suiteElement.addAttribute(new Attribute(ATTRIBUTE_EXPECTED_FAILURES, Integer.toString(child.getExpectedFailures())));
            suiteElement.addAttribute(new Attribute(ATTRIBUTE_FAILURES, Integer.toString(child.getFailures())));
            suiteElement.addAttribute(new Attribute(ATTRIBUTE_ERRORS, Integer.toString(child.getErrors())));
            suiteElement.addAttribute(new Attribute(ATTRIBUTE_SKIPPED, Integer.toString(child.getSkipped())));
            suiteElement.addAttribute(new Attribute(ATTRIBUTE_DURATION, Long.toString(child.getDuration())));

            root.appendChild(suiteElement);
        }

        for (PersistentTestCaseResult child : suite.getCases())
        {
            Element caseElement = new Element(ELEMENT_CASE);
            caseElement.addAttribute(safeAttribute(ATTRIBUTE_NAME, child.getName()));
            caseElement.addAttribute(new Attribute(ATTRIBUTE_DURATION, Long.toString(child.getDuration())));
            caseElement.addAttribute(new Attribute(ATTRIBUTE_STATUS, child.getStatusName()));

            String message = child.getMessage();
            if (message != null)
            {
                Element messageElement = safeElement(ELEMENT_MESSAGE, message);
                caseElement.appendChild(messageElement);
            }

            root.appendChild(caseElement);
        }

        return new Document(root);
    }

    private String base64Encode(String value)
    {
        try
        {
            return new String(Base64.encodeBase64(value.getBytes("UTF-8")), "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            return value;
        }
    }

    private Attribute safeAttribute(String name, String value)
    {
        try
        {
            return new Attribute(name, value);
        }
        catch(IllegalCharacterDataException e)
        {
            return new Attribute(ENCODED_PREFIX + name, base64Encode(value));
        }
    }

    private Element safeElement(String name, String text)
    {
        try
        {
            Element element = new Element(name);
            element.appendChild(text);
            return element;
        }
        catch (IllegalCharacterDataException e)
        {
            Element element = new Element(ENCODED_PREFIX + name);
            element.appendChild(base64Encode(text));
            return element;
        }
    }

    public PersistentTestSuiteResult read(String name, File directory, boolean deep, boolean failuresOnly, int limit) throws IOException, ParsingException
    {
        BuildingTestHandler handler = new BuildingTestHandler();
        read(handler, name, directory, deep, failuresOnly, new Counter(limit));
        return handler.getTop();
    }

    public void read(TestHandler handler, String name, File directory, boolean deep, boolean failuresOnly) throws IOException, ParsingException
    {
        read(handler, name, directory, deep, failuresOnly, new Counter(0));
    }

    private void read(TestHandler handler, String name, File directory, boolean deep, boolean failuresOnly, Counter counter) throws IOException, ParsingException
    {
        File suiteFile = new File(directory, SUITE_FILE_NAME);
        Document doc = readDoc(suiteFile);
        long duration = getDuration(doc.getRootElement());

        handler.startSuite(new PersistentTestSuiteResult(name, duration));
        loadSuites(handler, doc, directory, deep, failuresOnly, counter);
        loadCases(handler, doc, failuresOnly, counter);
        if(handler.endSuite())
        {
            // Document has changed, need to save.
            XMLUtils.writeDocument(suiteFile, doc);
        }
    }

    private void loadSuites(TestHandler handler, Document doc, File directory, boolean deep, boolean failuresOnly, Counter counter) throws IOException, ParsingException
    {
        Elements elements = doc.getRootElement().getChildElements(ELEMENT_SUITE);
        for (int i = 0; i < elements.size(); i++)
        {
            Element element = elements.get(i);
            String name = getSafeAttributeValue(element, ATTRIBUTE_NAME);
            long duration = getDuration(element);
            int total = getIntAttribute(element, ATTRIBUTE_TOTAL);
            int expectedFailures = getIntAttribute(element, ATTRIBUTE_EXPECTED_FAILURES);
            int errors = getIntAttribute(element, ATTRIBUTE_ERRORS);
            int failures = getIntAttribute(element, ATTRIBUTE_FAILURES);
            int skipped = getIntAttribute(element, ATTRIBUTE_SKIPPED);

            if (name == null || failuresOnly && (errors == 0 && failures == 0 && expectedFailures == 0))
            {
                continue;
            }

            if (deep)
            {
                File child = new File(directory, WebUtils.formUrlEncode(name));
                if (child.isDirectory())
                {
                    read(handler, name, child, deep, failuresOnly, counter);
                    if (counter.isExhausted())
                    {
                        return;
                    }
                }
            }
            else
            {
                handler.startSuite(new PersistentTestSuiteResult(name, duration, total, expectedFailures, errors, failures, skipped));
                handler.endSuite();
            }
        }
    }

    private String base64Decode(String value)
    {
        try
        {
            return new String(Base64.decodeBase64(value.getBytes("UTF-8")), "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            return value;
        }
    }

    private String getSafeAttributeValue(Element element, String name)
    {
        String result = element.getAttributeValue(name);
        if(result == null)
        {
            result = element.getAttributeValue(ENCODED_PREFIX + name);
            if(result != null)
            {
                result = base64Decode(result);
            }
        }

        return result;
    }

    private int getIntAttribute(Element element, String attribute)
    {
        int result = 0;
        String val = element.getAttributeValue(attribute);
        if (val != null)
        {
            try
            {
                result = Integer.parseInt(val);
            }
            catch (NumberFormatException e)
            {
                // 0 will do
            }
        }

        return result;
    }

    private void loadCases(TestHandler handler, Document doc, boolean failuresOnly, Counter counter)
    {
        Elements elements = doc.getRootElement().getChildElements(ELEMENT_CASE);
        for (int i = 0; i < elements.size(); i++)
        {
            Element element = elements.get(i);
            TestStatus status = getStatus(element);
            if (failuresOnly && !status.isBroken())
            {
                continue;
            }

            PersistentTestCaseResult caseResult = new PersistentTestCaseResult(getSafeAttributeValue(element, ATTRIBUTE_NAME), getDuration(element), status, getSafeText(element, ELEMENT_MESSAGE));
            if(caseResult.getStatus() == TestStatus.PASS)
            {
                caseResult.setFixed(element.getAttributeValue(ATTRIBUTE_FIXED) != null);
            }
            else
            {
                caseResult.setBrokenSince(getLongAttribute(element, ATTRIBUTE_BROKEN_SINCE));
                caseResult.setBrokenNumber(getLongAttribute(element, ATTRIBUTE_BROKEN_NUMBER));
            }

            handler.handleCase(caseResult, element);
            counter.add();
            if (counter.isExhausted())
            {
                return;
            }
        }
    }

    private long getLongAttribute(Element element, String attribute)
    {
        String value = element.getAttributeValue(attribute);
        if(value == null)
        {
            return 0;
        }

        try
        {
            return Long.parseLong(value);
        }
        catch(NumberFormatException e)
        {
            return 0;
        }
    }

    private long getDuration(Element element)
    {
        long result = TestResult.DURATION_UNKNOWN;
        String attributeValue = element.getAttributeValue(ATTRIBUTE_DURATION);

        if (attributeValue != null)
        {
            try
            {
                result = Long.parseLong(attributeValue);
            }
            catch (NumberFormatException e)
            {
                // Unknown will have to do.
            }
        }

        return result;
    }

    private TestStatus getStatus(Element element)
    {
        TestStatus status = TestStatus.PASS;
        String statusName = element.getAttributeValue(ATTRIBUTE_STATUS);
        if (statusName != null)
        {
            try
            {
                status = TestStatus.valueOf(statusName);
            }
            catch (IllegalArgumentException e)
            {
                // Pass is less damaging
            }
        }

        return status;
    }

    private String getSafeText(Element element, String name)
    {
        String message = XMLUtils.getChildText(element, name, null);
        if(message == null)
        {
            if(element.getFirstChildElement(name) != null)
            {
                // Actually there, but with an empty message
                return "";
            }
            
            message = XMLUtils.getChildText(element, ENCODED_PREFIX + name, null);
            if(message != null)
            {
                message = base64Decode(message);
            }
        }

        return message;
    }

    private Document readDoc(File file) throws IOException, ParsingException
    {
        Builder builder = new Builder();
        return builder.build(file);
    }

    private class Counter
    {
        public int limit;
        public int count;

        public Counter(int limit)
        {
            this.limit = limit;
            this.count = 0;
        }

        public void add()
        {
            count++;
        }

        public boolean isExhausted()
        {
            return limit > 0 && count >= limit;
        }
    }
}
