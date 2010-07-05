package com.zutubi.pulse.master.webwork.dispatcher;

import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.xwork.MockActionInvocation;
import com.opensymphony.xwork.util.OgnlValueStack;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import static com.zutubi.util.Constants.UTF8;
import org.displaytag.filter.SimpleServletOutputStream;
import static org.mockito.Mockito.mock;
import org.mortbay.http.HttpResponse;
import org.mortbay.jetty.servlet.ServletHttpRequest;
import org.mortbay.jetty.servlet.ServletHttpResponse;

import javax.servlet.ServletOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;

public class JsonResultTest extends PulseTestCase
{
    private JsonResult result;
    private OgnlValueStack stack;
    private FakeHttpServletResponse response;
    private MockActionInvocation ai;

    public JsonResultTest()
    {
    }

    public JsonResultTest(String name)
    {
        super(name);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        result = new JsonResult();
        result.setLocation("/");
        stack = new OgnlValueStack();
        response = new FakeHttpServletResponse();
        ai = new MockActionInvocation();
        ai.setStack(stack);
        ServletActionContext.setResponse(response);
    }

    public void testSinglePair() throws Exception
    {
        // set location.
        stack.push(new TestDataSource()
        {
            public String getString()
            {
                return "A";
            }
        });
        result.setDefinitionLoader(new SJDL("<?xml version=\"1.0\"?><object><pair key=\"key\">string</pair></object>"));
        result.execute(ai);

        assertEquals("{\"key\":\"A\"}", response.getOutputStreamContents());
    }

    public void testMultiplePairs() throws Exception
    {
        // set location.
        stack.push(new TestDataSource()
        {
            public String getString()
            {
                return "A";
            }
            public String getAnotherString()
            {
                return "B";
            }
        });
        result.setDefinitionLoader(new SJDL("<?xml version=\"1.0\"?>" +
                "<object>" +
                "<pair key=\"key\">string</pair>" +
                "<pair key=\"anotherKey\">anotherString</pair>" +
                "</object>"));
        result.execute(ai);

        assertEquals("{\"key\":\"A\",\"anotherKey\":\"B\"}", response.getOutputStreamContents());
    }

    public void testRetrieveDataFromNestedObject() throws Exception
    {
        // set location.
        stack.push(new TestDataSource()
        {
            public Object getA()
            {
                return new TestDataSource()
                {
                    public String getB()
                    {
                        return "B";
                    }
                };
            }
        });
        result.setDefinitionLoader(new SJDL("<?xml version=\"1.0\"?><object><pair key=\"key\">a.b</pair></object>"));
        result.execute(ai);

        assertEquals("{\"key\":\"B\"}", response.getOutputStreamContents());

    }

    public void testNestedObject() throws Exception
    {
        stack.push(new TestDataSource()
        {
            public Object getA()
            {
                return "A";
            }
        });
        result.setDefinitionLoader(new SJDL("<?xml version=\"1.0\"?><object><pair key=\"key\"><object><pair key=\"nestedKey\">a</pair></object></pair></object>"));
        result.execute(ai);

        assertEquals("{\"key\":{\"nestedKey\":\"A\"}}", response.getOutputStreamContents());
    }

    public void testArrayOfLiteralPrimitivesUsingCollection() throws Exception
    {
        stack.push(new TestDataSource()
        {
            public Object getA()
            {
                List<String> l = new LinkedList<String>();
                l.add("A");
                l.add("B");
                return l;
            }
        });
        result.setDefinitionLoader(new SJDL("<?xml version=\"1.0\"?><object><pair key=\"key\"><array ref=\"a\"/></pair></object>"));
        result.execute(ai);

        assertEquals("{\"key\":[\"A\",\"B\"]}", response.getOutputStreamContents());
    }

    public void testArrayOfLiteralPrimitivesUsingArray() throws Exception
    {
        stack.push(new TestDataSource()
        {
            public Object getA()
            {
                String[] a = new String[2];
                a[0] = "A";
                a[1] = "B";
                return a;
            }
        });
        result.setDefinitionLoader(new SJDL("<?xml version=\"1.0\"?><object><pair key=\"key\"><array ref=\"a\"/></pair></object>"));
        result.execute(ai);

        assertEquals("{\"key\":[\"A\",\"B\"]}", response.getOutputStreamContents());
    }

    public void testArrayOfEvaluatedPrimitives() throws Exception
    {
        stack.push(new TestDataSource()
        {
            public Object getA()
            {
                List<Object> l = new LinkedList<Object>();
                l.add(new ABC("A"));
                l.add(new ABC("B"));
                return l;
            }
        });
        result.setDefinitionLoader(new SJDL("<?xml version=\"1.0\"?><object><pair key=\"key\"><array ref=\"a\">a</array></pair></object>"));
        result.execute(ai);

        assertEquals("{\"key\":[\"A\",\"B\"]}", response.getOutputStreamContents());
    }

    public void testArrayOfObjects() throws Exception
    {
        stack.push(new TestDataSource()
        {
            public Object getA()
            {
                List<Object> l = new LinkedList<Object>();
                l.add(new ABC("A", "a"));
                l.add(new ABC("B", "b"));
                return l;
            }
        });
        result.setDefinitionLoader(new SJDL("<?xml version=\"1.0\"?>" +
                "<object>" +
                    "<pair key=\"key\">" +
                        "<array ref=\"a\">" +
                            "<object>" +
                                "<pair key=\"akey\">a</pair>" +
                                "<pair key=\"bkey\">b</pair>" +
                            "</object>" +
                        "</array>" +
                    "</pair>" +
                "</object>"));
        result.execute(ai);

        assertEquals("{\"key\":[{\"akey\":\"A\",\"bkey\":\"a\"},{\"akey\":\"B\",\"bkey\":\"b\"}]}", response.getOutputStreamContents());
    }

    public void testMapOfStrings() throws Exception
    {
        stack.push(new TestDataSource()
        {
            public Object getA()
            {
                Map<String, String> map = new HashMap<String, String>();
                map.put("A", "B");
                map.put("C", "D");
                return map;
            }
        });
        result.setDefinitionLoader(new SJDL("<?xml version=\"1.0\"?><object><pair key=\"key\"><map ref=\"a\"></map></pair></object>"));
        result.execute(ai);
        assertEquals("{\"key\":{\"A\":\"B\",\"C\":\"D\"}}", response.getOutputStreamContents());
    }

    public void testMapOfLists() throws Exception
    {
        stack.push(new TestDataSource()
        {
            public Object getA()
            {
                Map<String, List<String>> map = new HashMap<String, List<String>>();
                map.put("A", Arrays.asList("A", "B", "C"));
                map.put("C", Arrays.asList("D", "E", "F"));
                return map;
            }
        });
        result.setDefinitionLoader(new SJDL("<?xml version=\"1.0\"?>" +
                "<object>" +
                    "<pair key=\"key\"><map ref=\"a\"><array/></map></pair>" +
                "</object>"
        ));
        result.execute(ai);
        assertEquals("{\"key\":{\"A\":[\"A\",\"B\",\"C\"],\"C\":[\"D\",\"E\",\"F\"]}}", response.getOutputStreamContents());
    }

    public void testActionErrorDefinition() throws Exception
    {
        ActionSupport action = new ActionSupport();
        action.addActionError("action error a");
        action.addActionError("action error b");
        stack.push(action);

        result.setDefinitionLoader(new SJDL("<?xml version=\"1.0\"?>" +
                "<object>" +
                    "<pair key=\"actionError\">" +
                        "<array ref=\"actionErrors\"></array>" +
                    "</pair>" +
                "</object>"));
        result.execute(ai);

        assertEquals("{\"actionError\":[\"action error a\",\"action error b\"]}", response.getOutputStreamContents());
    }

    public void testActionFieldErrors() throws Exception
    {
        ActionSupport action = new ActionSupport();
        action.addFieldError("a", "error a 1");
        action.addFieldError("a", "error a 2");
        action.addFieldError("b", "error b");
        stack.push(action);

        result.setDefinitionLoader(new SJDL("<?xml version=\"1.0\"?>" +
                "<object>" +
                    "<pair key=\"fieldError\"><map ref=\"fieldErrors\"><array/></map></pair>" +
                "</object>"));
        result.execute(ai);

        String contents = response.getOutputStreamContents();
        assertTrue(contents.contains("\"b\":[\"error b\"]"));
        assertTrue(contents.contains("\"a\":[\"error a 1\",\"error a 2\"]"));
    }

    interface TestDataSource
    {
    }

    class ABC
    {
        private String a;
        private String b;
        private String c;

        public ABC(String a)
        {
            this.a = a;
        }

        public ABC(String a, String b)
        {
            this.a = a;
            this.b = b;
        }

        public ABC(String a, String b, String c)
        {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        public String getA()
        {
            return a;
        }

        public String getB()
        {
            return b;
        }

        public String getC()
        {
            return c;
        }
    }

    class SJDL implements JsonDefinitionLoader
    {
        private String def;
        public SJDL(String def)
        {
            this.def = def;
        }

        public InputStream load(String location)
        {
            return new ByteArrayInputStream(def.getBytes());
        }
    }

    private static class FakeHttpServletResponse extends ServletHttpResponse
    {
        private String encoding = UTF8;
        private SimpleServletOutputStream outputStream = null;

        public FakeHttpServletResponse()
        {
            super(mock(ServletHttpRequest.class), mock(HttpResponse.class));
        }

        public String getCharacterEncoding()
        {
            return encoding;
        }

        public ServletOutputStream getOutputStream()
        {
            if (outputStream == null)
            {
                outputStream = new SimpleServletOutputStream();
            }

            return outputStream;
        }

        public String getOutputStreamContents()
        {
            return outputStream.toString();
        }

        public String getContentType()
        {
            return "text/plain";
        }

        public void setCharacterEncoding(String str)
        {
            this.encoding = str;
        }
    }
}
