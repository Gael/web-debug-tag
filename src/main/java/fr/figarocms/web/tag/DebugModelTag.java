package fr.figarocms.web.tag;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class DebugModelTag extends TagSupport {
    private static final long serialVersionUID = 4611181048692549740L;

    private static final String SCRIPT_TYPE_TEXT_JAVASCRIPT_START = "<script type=\"text/javascript\">";

    private static final String SCRIPT_END = "</script>";

    /**
     * Property -Ddebug.jsp = true à  to set in JVM variables at launch.
     */
    private static final String DEBUG_JSP_FLAG = "debug.jsp";

    private static final String SINGLE_QUOTE = "'";

    private static final String EMPTY = "";

    private static final String VAR_JS_ATTRIBUTE_VIEWER = "attributeViewer";

    private static final String VAR = "var ";

    private static final String STRING_CLASS_NAME = "java.lang.String";

    public static final String WEBDEBUG_EXCLUDES = "webdebug.excludes";

    private static final String PAGE_REQUEST_KEY = "page";

    private static final String REQUEST_MODEL_KEY = "request";

    private static final String SESSION_MODEL_KEY = "session";

    private static final String APPLICATION_MODEL_KEY = "application";

    private static final String EXCLUDE_PACKAGE_SEPARATOR = ",";

    private static final Logger LOGGER = LoggerFactory.getLogger(DebugModelTag.class);

    private static final String DEBUG_JVM_PARAMETER = System.getProperty(DEBUG_JSP_FLAG);


    private static final boolean DEBUG_FLAG =
            (DEBUG_JVM_PARAMETER != null) && Boolean.parseBoolean(DEBUG_JVM_PARAMETER);

    private static final List<Integer> SCOPES = Lists.newArrayList(
            Arrays.asList(PageContext.PAGE_SCOPE, PageContext.SESSION_SCOPE, PageContext.REQUEST_SCOPE, PageContext.APPLICATION_SCOPE));


    @Override
    public int doStartTag() throws JspException {

        boolean debugOk = DEBUG_FLAG;
        if (debugOk) {
            outputDebugModelInJSON();
        }

        return SKIP_BODY;
    }

    protected void outputDebugModelInJSON() throws JspException {

        JspWriter out = pageContext.getOut();

        try {
            List<String> classesToExclude = getClassesToExcludes();
            Map<String, Object> debugModel = buildMapOfAttributesToSerialize(pageContext);
            String debugModelAsJSON = ToJSON(classesToExclude, debugModel);
            printStringWithJSPWriter(out, debugModelAsJSON);
        } catch (IOException e) {
            throw new JspException("IOException while writing data to page" + e.getMessage(), e);
        }

    }

    private List<String> getClassesToExcludes() {
        String packagesToExclude = pageContext.getServletContext().getInitParameter(WEBDEBUG_EXCLUDES);
        List<String> classesToExclude = Lists.newArrayList();
        if (packagesToExclude != null) {
            classesToExclude = Arrays.asList(packagesToExclude.split(EXCLUDE_PACKAGE_SEPARATOR));
        }
        return classesToExclude;
    }

    private String ToJSON(List<String> tokenToFilter, Map<String, Object> debugModel) {
        ObjectMapper objectMapper = getObjectMapper(tokenToFilter);
        String debugModelAsJSON = null;
        try {
            debugModelAsJSON = objectMapper.writeValueAsString(debugModel);
        } catch (Throwable t) {
            LOGGER.error("error in debugModel serialization in JSON", t);
        }
        return debugModelAsJSON;
    }

    private Map<String, Object> buildMapOfAttributesToSerialize(PageContext pageContext) {
        Map<String, Object> debugPage = Maps.newHashMap();
        Map<String, Object> debugRequest = Maps.newHashMap();
        Map<String, Object> debugSession = Maps.newHashMap();
        Map<String, Object> debugApplication = Maps.newHashMap();
        Map<String, Object> debugModel = Maps.newHashMap();

        for (Integer scope : SCOPES) {
            Enumeration attributeNames = pageContext.getAttributeNamesInScope(scope);

            while (attributeNames != null && attributeNames.hasMoreElements()) {
                String element = attributeNames.nextElement().toString();
                Object attribute = null;

                if (element != null) {

                    if (scope == PageContext.PAGE_SCOPE) {
                        attribute = pageContext.getAttribute(element);
                        if (attribute != null) {
                            addAttributeToMap(element, attribute, debugPage);
                        }
                    } else if (scope == PageContext.REQUEST_SCOPE) {
                        attribute = pageContext.getRequest().getAttribute(element);
                        if (attribute != null) {
                            addAttributeToMap(element, attribute, debugRequest);
                        }
                    } else if (scope == PageContext.SESSION_SCOPE) {
                        final HttpSession session = pageContext.getSession();
                        if (session != null) {
                            attribute = session.getAttribute(element);
                        }
                        if (attribute != null) {
                            addAttributeToMap(element, attribute, debugSession);
                        }
                    } else if (scope == PageContext.APPLICATION_SCOPE) {
                        attribute = pageContext.getServletContext().getAttribute(element);
                        if (attribute != null) {
                            addAttributeToMap(element, attribute, debugApplication);
                        }
                    }
                }
            }
        }
        debugModel.put(PAGE_REQUEST_KEY, debugPage);
        debugModel.put(REQUEST_MODEL_KEY, debugRequest);
        debugModel.put(SESSION_MODEL_KEY, debugSession);
        debugModel.put(APPLICATION_MODEL_KEY, debugApplication);

        return debugModel;
    }

    private ObjectMapper getObjectMapper(List<String> tokenToFilter) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(
                JsonAutoDetect.Visibility.ANY));
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        PropertyFilteringModule.Builder builder = PropertyFilteringModule.builder("Module Name");
        for (String token : tokenToFilter) {
            builder.exclude(Pattern.compile(token));
        }
        objectMapper.registerModule(builder.build());
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC);
        objectMapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC);
        return objectMapper;
    }

    private void printStringWithJSPWriter(final JspWriter out, final String debugModelAsJSON) throws IOException {
        out.println(SCRIPT_TYPE_TEXT_JAVASCRIPT_START);
        String stringToJSONify = debugModelAsJSON;
        if (stringToJSONify != null && stringToJSONify.isEmpty()) {
            stringToJSONify = null;
        }
        stringToJSONify = Objects.firstNonNull(stringToJSONify, "null");

        out.println(VAR + VAR_JS_ATTRIBUTE_VIEWER + " = " +
                stringToJSONify.replaceAll(SINGLE_QUOTE, EMPTY) + ";");
        out.println("(typeof console === \"undefined\")? {} : console.dir(" + VAR_JS_ATTRIBUTE_VIEWER + ");");
        out.println(SCRIPT_END);
    }

    private void addAttributeToMap(final String element, final Object attribute, Map<String, Object> map) {
        if (attribute.getClass().getCanonicalName().equals(STRING_CLASS_NAME)) {
            map.put(element, StringEscapeUtils.escapeHtml(attribute.toString()));
        } else {
            map.put(element, attribute);
        }
    }

}
