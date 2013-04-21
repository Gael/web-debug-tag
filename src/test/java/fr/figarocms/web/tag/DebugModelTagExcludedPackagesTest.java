package fr.figarocms.web.tag;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockPageContext;

import javax.servlet.jsp.JspException;

import static org.fest.assertions.Assertions.assertThat;

public class DebugModelTagExcludedPackagesTest extends AbstractDebugModelTagTest{


    public static final String NO_ATTRIBUTES_SERIALIZED = "<script type=\"text/javascript\">\n" +
            "var attributeViewer = null;\n" +
            "(typeof console === \"undefined\")? {} : console.dir(attributeViewer);\n" +
            "</script>\n";
    public static final String DUMMY_VALUES_WITH_JAVA_UTIL_LOGGING_EXCLUSION = "<script type=\"text/javascript\">\n" +
            "var attributeViewer = {\"application\":{\"javax.servlet.context.tempdir\":\"/tmp\"},\"session\":{\"dummySessionKey\":\"toto\",\"fsdf\":\"sdfsfd\"},\"page\":{},\"request\":{\"dsdfsdf\":{\"toto\":\"dummy value\",\"titi\":{\"dummyInteger\":8,\"map\":{\"test\":\"another dummy data\"}}}}};\n" +
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
        servletContext.addInitParameter(DebugModelTag.WEBDEBUG_EXCLUDES,"fr.figarocms.web.tag");
        session.setAttribute("dummySessionKey",this);
        //when
        mockDebugModelTag.doStartTag();
        //then
        assertThat(new String(pageContext.getContentAsByteArray())).isEqualTo(NO_ATTRIBUTES_SERIALIZED);
    }


    @Test
    public void testExcludedPackagesWith_java_util_logging_exclusion() throws JspException {
        //given
        servletContext.addInitParameter(DebugModelTag.WEBDEBUG_EXCLUDES,"java.util.logging.*");
        session.setAttribute("dummySessionKey", "toto");
        request.setAttribute("dsdfsdf",new fr.figarocms.web.tag.Test());
        pageContext.setAttribute("fsdf","sdfsfd",3);
        mockDebugModelTag = new DebugModelTag();
        mockDebugModelTag.setPageContext(pageContext);

        //when
        mockDebugModelTag.doStartTag();
        //then
        assertThat(new String(pageContext.getContentAsByteArray())).isEqualTo(DUMMY_VALUES_WITH_JAVA_UTIL_LOGGING_EXCLUSION);
    }



}

