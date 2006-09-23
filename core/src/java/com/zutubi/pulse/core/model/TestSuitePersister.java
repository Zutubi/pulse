package com.zutubi.pulse.core.model;

import com.zutubi.pulse.util.FileSystemUtils;
import com.zutubi.pulse.util.IOUtils;
import com.zutubi.pulse.util.logging.Logger;
import nu.xom.*;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 */
public class TestSuitePersister
{
    private static final Logger LOG = Logger.getLogger(TestSuitePersister.class);

    private static final String SUITE_FILE_NAME = "suite.xml";
    private static final String ELEMENT_SUITE = "suite";
    private static final String ELEMENT_CASE = "case";
    private static final String ELEMENT_MESSAGE = "message";
    private static final String ATTRIBUTE_TOTAL = "total";
    private static final String ATTRIBUTE_FAILURES = "failures";
    private static final String ATTRIBUTE_ERRORS = "errors";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_DURATION = "duration";
    private static final String ATTRIBUTE_STATUS = "status";


    public void write(TestSuiteResult suite, File directory) throws IOException
    {
        for(TestSuiteResult childSuite: suite.getSuites())
        {
            File suiteDir = new File(directory, encodeName(childSuite.getName()));
            FileSystemUtils.createDirectory(suiteDir);

            write(childSuite, suiteDir);
        }

        writeCases(suite, new File(directory, SUITE_FILE_NAME));
    }

    private void writeCases(TestSuiteResult suite, File file) throws IOException
    {
        Document doc = suiteToDoc(suite);
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;

        try
        {
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);

            Serializer serializer = new Serializer(bos);
            serializer.write(doc);
        }
        finally
        {
            IOUtils.close(bos);
        }
    }

    private Document suiteToDoc(TestSuiteResult suite)
    {
        Element root = new Element(ELEMENT_SUITE);
        root.addAttribute(new Attribute(ATTRIBUTE_DURATION, Long.toString(suite.getDuration())));

        for(TestSuiteResult child: suite.getSuites())
        {
            Element suiteElement = new Element(ELEMENT_SUITE);
            suiteElement.addAttribute(new Attribute(ATTRIBUTE_NAME, child.getName()));
            suiteElement.addAttribute(new Attribute(ATTRIBUTE_TOTAL, Integer.toString(child.getTotal())));
            suiteElement.addAttribute(new Attribute(ATTRIBUTE_FAILURES, Integer.toString(child.getFailures())));
            suiteElement.addAttribute(new Attribute(ATTRIBUTE_ERRORS, Integer.toString(child.getErrors())));
            suiteElement.addAttribute(new Attribute(ATTRIBUTE_DURATION, Long.toString(child.getDuration())));

            root.appendChild(suiteElement);
        }

        for(TestCaseResult child: suite.getCases())
        {
            Element caseElement = new Element(ELEMENT_CASE);
            caseElement.addAttribute(new Attribute(ATTRIBUTE_NAME, child.getName()));
            caseElement.addAttribute(new Attribute(ATTRIBUTE_DURATION, Long.toString(child.getDuration())));
            caseElement.addAttribute(new Attribute(ATTRIBUTE_STATUS, child.getStatusName()));

            String message = child.getMessage();
            if(message != null)
            {
                Element messageElement = new Element(ELEMENT_MESSAGE);
                messageElement.appendChild(message);
                caseElement.appendChild(messageElement);
            }

            root.appendChild(caseElement);
        }

        return new Document(root);
    }

    public TestSuiteResult read(String name, File directory, boolean deep, boolean failuresOnly, int limit) throws IOException, ParsingException
    {
        return read(name, directory, deep, failuresOnly, new Counter(limit));
    }

    public TestSuiteResult read(String name, File directory, boolean deep, boolean failuresOnly, Counter counter) throws IOException, ParsingException
    {
        File suiteFile = new File(directory, SUITE_FILE_NAME);
        Document doc = readDoc(suiteFile);
        long duration = getDuration(doc.getRootElement());

        TestSuiteResult suite = new TestSuiteResult(name, duration);
        loadSuites(suite, doc, directory, deep, failuresOnly, counter);
        loadCases(suite, doc, failuresOnly, counter);

        return suite;
    }

    private void loadSuites(TestSuiteResult suite, Document doc, File directory, boolean deep, boolean failuresOnly, Counter counter) throws IOException, ParsingException
    {
        Elements elements = doc.getRootElement().getChildElements(ELEMENT_SUITE);
        for(int i = 0; i < elements.size(); i++)
        {
            Element element = elements.get(i);
            String name = element.getAttributeValue(ATTRIBUTE_NAME);
            long duration = getDuration(element);
            int total = getIntAttribute(element, ATTRIBUTE_TOTAL);
            int errors = getIntAttribute(element, ATTRIBUTE_ERRORS);
            int failures = getIntAttribute(element, ATTRIBUTE_FAILURES);

            if(name == null || failuresOnly && (errors == 0 && failures == 0))
            {
                continue;
            }

            if(deep)
            {
                File child = new File(directory, encodeName(name));
                if(child.isDirectory())
                {
                    suite.add(read(name, child, deep, failuresOnly, counter));
                    if(counter.isExhausted())
                    {
                        return;
                    }
                }
            }
            else
            {
                suite.add(new TestSuiteResult(name, duration, total, errors, failures));
            }
        }
    }

    private int getIntAttribute(Element element, String attribute)
    {
        int result = 0;
        String val = element.getAttributeValue(attribute);
        if(val != null)
        {
            try
            {
                result = Integer.parseInt(val);
            }
            catch(NumberFormatException e)
            {
                // 0 will do
            }
        }

        return result;
    }

    private void loadCases(TestSuiteResult suite, Document doc, boolean failuresOnly, Counter counter)
    {
        Elements elements = doc.getRootElement().getChildElements(ELEMENT_CASE);
        for(int i = 0; i < elements.size(); i++)
        {
            Element element = elements.get(i);
            TestCaseResult.Status status = getStatus(element);
            if(failuresOnly && status == TestCaseResult.Status.PASS)
            {
                continue;
            }

            suite.add(new TestCaseResult(element.getAttributeValue(ATTRIBUTE_NAME), getDuration(element), status, getMessage(element)));
            counter.add();
            if(counter.isExhausted())
            {
                return;
            }
        }
    }

    private long getDuration(Element element)
    {
        long result = TestResult.UNKNOWN_DURATION;
        String attributeValue = element.getAttributeValue(ATTRIBUTE_DURATION);

        if(attributeValue != null)
        {
            try
            {
                result = Long.parseLong(attributeValue);
            }
            catch(NumberFormatException e)
            {
                // Unknown will have to do.
            }
        }

        return result;
    }

    private TestCaseResult.Status getStatus(Element element)
    {
        TestCaseResult.Status status = TestCaseResult.Status.PASS;
        String statusName = element.getAttributeValue(ATTRIBUTE_STATUS);
        if(statusName != null)
        {
            try
            {
                status = TestCaseResult.Status.valueOf(statusName);
            }
            catch(IllegalArgumentException e)
            {
                // Pass is less damaging
            }
        }

        return status;
    }

    private String getMessage(Element element)
    {
        Elements messageElements = element.getChildElements(ELEMENT_MESSAGE);
        if(messageElements.size() > 0 && messageElements.get(0).getChildCount() > 0)
        {
            Node child = messageElements.get(0).getChild(0);
            if(child instanceof Text)
            {
                return child.getValue();
            }
        }

        return null;
    }

    private Document readDoc(File file) throws IOException, ParsingException
    {
        Builder builder = new Builder();
        return builder.build(file);
    }

    private String encodeName(String name)
    {
        try
        {
            return URLEncoder.encode(name, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            return name;
        }
    }

    private String decodeName(String name)
    {
        try
        {
            return URLDecoder.decode(name, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            return name;
        }
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
