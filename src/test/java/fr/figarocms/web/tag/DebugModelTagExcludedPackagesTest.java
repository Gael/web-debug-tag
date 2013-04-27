package fr.figarocms.web.tag;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockPageContext;

import javax.servlet.jsp.JspException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.fest.assertions.Assertions.assertThat;

public class DebugModelTagExcludedPackagesTest extends AbstractDebugModelTagTest{


    public static final String NO_ATTRIBUTES_SERIALIZED = "<script type=\"text/javascript\">\n" +
            "var attributeViewer = {\"application\":{},\"session\":{\"dummySessionKey\":{}},\"page\":{},\"request\":{}};\n" +
            "(typeof console === \"undefined\")? {} : console.dir(attributeViewer);\n" +
            "</script>\n";
    public static final String DUMMY_VALUES_WITH_JAVA_UTIL_LOGGING_EXCLUSION = "<script type=\"text/javascript\">\n" +
            "var attributeViewer = {\"application\":{\"javax.servlet.context.tempdir\":\"/tmp\"},\"session\":{\"dummySessionKey\":\"toto\",\"fsdf\":\"sdfsfd\"},\"page\":{},\"request\":{\"dsdfsdfq\":{},\"dsdfsdf\":{\"toto\":\"dummy value\",\"titi\":{\"dummyInteger\":8}}}};\n" +
            "(typeof console === \"undefined\")? {} : console.dir(attributeViewer);\n" +
            "</script>\n";
    public static final String JAVA_UTIL_LOGGING_PATTERN = "java.util.logging.*";
    public static final String TEST_CLASS_PATTERN = "fr.figarocms.web.tag.Test";
    public static final String EXPECTED_RESULT_WITH_KEY_AND_VALUE_AS_STRING = "<script type=\"text/javascript\">\n" +
            "var attributeViewer = {\"application\":{},\"session\":{\"dummySessionKey\":{\"toto\":\"dummy value\",\"titi\":{\"dummyInteger\":8,\"map\":{\"test\":\"another dummy data\"}}}},\"page\":{},\"request\":{}};\n" +
            "(typeof console === \"undefined\")? {} : console.dir(attributeViewer);\n" +
            "</script>\n";

    public static final String EXPECTED_RESULT_WITH_ONLY_KEY = "<script type=\"text/javascript\">\n" +
            "var attributeViewer = {\"application\":{},\"session\":{\"dummySessionKey\":{}},\"page\":{},\"request\":{}};\n" +
            "(typeof console === \"undefined\")? {} : console.dir(attributeViewer);\n" +
            "</script>\n";

    public static final String EXPECTED_RESULT_WITH_KEY_AND_DUMMY_VALUE = "<script type=\"text/javascript\">\n" +
            "var attributeViewer = {\"application\":{},\"session\":{\"dummySessionKey\":\"toto\"},\"page\":{},\"request\":{}};\n" +
            "(typeof console === \"undefined\")? {} : console.dir(attributeViewer);\n" +
            "</script>\n";


    @Before
    public void setUp() throws Exception {
        super.setUp();
        pageContext = new MockPageContext(servletContext,request,response);
        mockDebugModelTag = new DebugModelTag();
        mockDebugModelTag.setPageContext(pageContext);
        overrideConstantField(field, true);

    }



    @Test
    public void testExcludedPackagesWith_exclusion_from_the_package_of_the_object_included() throws JspException {
        //given
        servletContext.addInitParameter(DebugModelTag.WEBDEBUG_EXCLUDES,"fr.figarocms.web.tag.*");
        cleanServletContextAttributes();
        session.setAttribute("dummySessionKey",this);
        //when
        mockDebugModelTag.doStartTag();
        //then
        assertThat(new String(pageContext.getContentAsByteArray())).isEqualTo(NO_ATTRIBUTES_SERIALIZED);
    }


    @Test
    public void testExcludedPackagesWith_java_util_logging_exclusion() throws JspException {
        //given
        servletContext.addInitParameter(DebugModelTag.WEBDEBUG_EXCLUDES, JAVA_UTIL_LOGGING_PATTERN);
        session.setAttribute("dummySessionKey", "toto");
        request.setAttribute("dsdfsdf",new fr.figarocms.web.tag.Test());
        request.setAttribute("dsdfsdfq",this);
        pageContext.setAttribute("fsdf","sdfsfd",3);
        mockDebugModelTag = new DebugModelTag();
        mockDebugModelTag.setPageContext(pageContext);

        //when
        mockDebugModelTag.doStartTag();
        //then
        assertThat(new String(pageContext.getContentAsByteArray())).isEqualTo(DUMMY_VALUES_WITH_JAVA_UTIL_LOGGING_EXCLUSION);
    }

    @Test
    public void testExcludedPackagesWith_java_util_logging_exclusion_and_one_key_and_one_value_as_string() throws JspException {
        //given
        cleanServletContextAttributes();
        servletContext.addInitParameter(DebugModelTag.WEBDEBUG_EXCLUDES, JAVA_UTIL_LOGGING_PATTERN);
        session.setAttribute("dummySessionKey", "toto");

        mockDebugModelTag = new DebugModelTag();
        mockDebugModelTag.setPageContext(pageContext);

        //when
        mockDebugModelTag.doStartTag();
        //then
        assertThat(new String(pageContext.getContentAsByteArray())).isEqualTo(EXPECTED_RESULT_WITH_KEY_AND_DUMMY_VALUE);
    }


    @Test
    public void testExcludedPackagesWith_test_class_excluded() throws JspException {
        //given
        cleanServletContextAttributes();
        servletContext.addInitParameter(DebugModelTag.WEBDEBUG_EXCLUDES, TEST_CLASS_PATTERN);
        session.setAttribute("dummySessionKey", new fr.figarocms.web.tag.Test());

        mockDebugModelTag = new DebugModelTag();
        mockDebugModelTag.setPageContext(pageContext);

        //when
        mockDebugModelTag.doStartTag();
        //then
        assertThat(new String(pageContext.getContentAsByteArray())).isEqualTo(EXPECTED_RESULT_WITH_ONLY_KEY);
    }

    @Test
    public void testRegexp(){
        Pattern compile = Pattern.compile(".*sun.*");
        Matcher matcher = compile.matcher("sun.nio.cs.StreamEncoder");
        Matcher matcher2 = compile.matcher("sun.misc.AppClassLoader[\"parent\"]");
        Matcher matcher3 = compile.matcher("Lsun.misc.AppClassLoader[\"parent\"]");

        assertThat(matcher.matches()).isTrue();
        assertThat(matcher2.matches()).isTrue();
        assertThat(matcher3.matches()).isTrue();
    }

    private void cleanServletContextAttributes() {
        Enumeration<String> attributeNames = servletContext.getAttributeNames();

        while(attributeNames.hasMoreElements()){
            servletContext.removeAttribute(attributeNames.nextElement());
        }
    }


}

