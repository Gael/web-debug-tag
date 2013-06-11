package fr.figarocms.web.tag;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class DebugModelTagWithSpecialJSONCharactersTest extends AbstractDebugModelTagTest {

    public static final String JSON_WITH_ESCAPED_SPECIAL_CHARACTERS = "<script type=\"text/javascript\">\n" +
            "var attributeViewer = {\"application\":{\"javax.servlet.context.tempdir\":\"/tmp\"},\"page\":{},\"session\":{},\"request\":{\"dummyAttribute\":\"&lt; &gt;\"}};\n" +
            "(typeof console === \"undefined\")? {} : console.dir(attributeViewer);\n" +
            "</script>\n";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        overrideConstantField(field, true);
        request.setAttribute("dummyAttribute", "< >");
    }


    @Test
    public void testSpecialCharactersAreEscaped() throws Exception {

        mockDebugModelTag = new DebugModelTag();
        mockDebugModelTag.setPageContext(pageContext);


        //when
        mockDebugModelTag.doStartTag();
        //then
        assertThat(new String(pageContext.getContentAsByteArray())).isEqualTo(JSON_WITH_ESCAPED_SPECIAL_CHARACTERS);

    }
}
