package fr.figarocms.web.tag;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fr.figarocms.web.tag.scopes.Scope;
import fr.figarocms.web.tag.scopes.Scopes;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public static final String DISABLE_TAG_INSTRUCTION = "to disable this tag, remove the command line '-D" + DEBUG_JSP_FLAG + "' argument of your application server ";

    private static final String SINGLE_QUOTE = "'";

    private static final String EMPTY = "";

    private static final String VAR_JS_ATTRIBUTE_VIEWER = "attributeViewer";

    private static final String VAR = "var ";

    private static final String STRING_CLASS_NAME = "java.lang.String";

    public static final String WEBDEBUG_EXCLUDES = "webdebug.excludes";


    private static final String EXCLUDE_PACKAGE_SEPARATOR = ",";

    private static final Logger LOGGER = LoggerFactory.getLogger(DebugModelTag.class);

    private static final String DEBUG_JVM_PARAMETER = System.getProperty(DEBUG_JSP_FLAG);


    private static final boolean DEBUG_FLAG =
            (DEBUG_JVM_PARAMETER != null) && Boolean.parseBoolean(DEBUG_JVM_PARAMETER);


    private static final String DUMMY_MODULE_NAME = "Module Name";
    public static final String SECURITY_WARNING_WHEN_WEB_DEBUG_TAG_IS_ACTIVATED = "web debug tag is enabled. do NOT activate this tag on PRODUCTION to avoid SECURITY ISSUES! ";

    private ObjectMapper objectMapper;


    @Override
    public int doStartTag() throws JspException {

        if (DEBUG_FLAG) {
            LOGGER.info(SECURITY_WARNING_WHEN_WEB_DEBUG_TAG_IS_ACTIVATED);
            LOGGER.info(DISABLE_TAG_INSTRUCTION);
            logInstructionsToExcludeFromSerializationSomeClasses();
            outputDebugModelInJSON();
        }

        return SKIP_BODY;
    }

    private void logInstructionsToExcludeFromSerializationSomeClasses() {
        LOGGER.info("to exclude some classes from serialization (if some JSONMappingException are thrown), put in your web.xml this context param markup for example:");
        LOGGER.info(" <context-param>");
        LOGGER.info("<param-name>webdebug.excludes</param-name>");
        LOGGER.info("<param-value>__spring*,__sitemesh*</param-name>");
        LOGGER.info("</context-param>");
    }

    protected void outputDebugModelInJSON() throws JspException {

        JspWriter out = pageContext.getOut();

        try {
            List<String> classesToExclude = getClassesToExcludes();
            Map<String, Object> debugModel = buildMapOfAttributesToSerialize(pageContext);
            String debugModelAsJSON = toJSON(classesToExclude, debugModel);
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

    private String toJSON(List<String> tokenToFilter, Map<String, Object> debugModel) {
        if (objectMapper == null) {
            objectMapper = getObjectMapper(tokenToFilter);
        }
        String debugModelAsJSON = null;
        try {
            debugModelAsJSON = objectMapper.writeValueAsString(debugModel);
        } catch (Throwable t) {
            LOGGER.error("error in debugModel serialization in JSON", t);
        }
        return debugModelAsJSON;
    }

    private Map<String, Object> buildMapOfAttributesToSerialize(PageContext pageContext) {
        Map<String, Object> debugModel = Maps.newHashMap();

        for (Scopes scopes : Scopes.values()) {
            Scope scopeItem = scopes.getScope();
            Enumeration attributeNames = pageContext.getAttributeNamesInScope(scopeItem.getScopeIdentifier());
            Map<String, Object> model = Maps.newHashMap();
            while (attributeNames != null && attributeNames.hasMoreElements()) {
                Optional<String> attributeName = Optional.fromNullable(attributeNames.nextElement().toString());
                Optional<Object> attributeValue = scopeItem.getAttributeValue(pageContext, attributeName);
                addAttributeToMap(attributeName, attributeValue, model);
            }
            debugModel.put(scopes.getKey(), model);
        }


        return debugModel;
    }

    private ObjectMapper getObjectMapper(List<String> tokenToFilter) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(
                JsonAutoDetect.Visibility.ANY));
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        PropertyFilteringModule.Builder builder = PropertyFilteringModule.builder(DUMMY_MODULE_NAME);
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

    private void addAttributeToMap(final Optional<String> attributeName, final Optional<Object> attributeValue, Map<String, Object> map) {
        if (!attributeName.isPresent() || !attributeValue.isPresent()) {
            return;
        }
        if (attributeValue.getClass().getCanonicalName().equals(STRING_CLASS_NAME)) {
            map.put(attributeName.get(), StringEscapeUtils.escapeHtml(attributeValue.get().toString()));
        } else {
            map.put(attributeName.get(), attributeValue.get());
        }
    }

}
