package fr.figarocms.web.tag;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * code adapted from https://github.com/ultraflynn/jackson-custom-serialization .
 */
public final class FilteringBeanSerializerModifier
        extends BeanSerializerModifier {
    private Pattern javaUtilLoggingPattern = Pattern.compile(".*java.util.logging.*");
    private Pattern sunPattern = Pattern.compile(".*sun.*");
    private Pattern objectPattern = Pattern.compile(".*java.lang.Object.*");
    private Set<Pattern> filters = Sets.newHashSet(javaUtilLoggingPattern, sunPattern, objectPattern);

    private static Logger LOGGER = LoggerFactory.getLogger(FilteringBeanSerializerModifier.class);

    static FilteringBeanSerializerModifier excluding(Set<Pattern> filters) {
        return new FilteringBeanSerializerModifier(filters);
    }

    private FilteringBeanSerializerModifier(Set<Pattern> filters) {
        this.filters.addAll(filters);
    }

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                     BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
        if (filters == null || filters.isEmpty()) return beanProperties;


        Class<?> beanClass = beanDesc.getBeanClass();
        LOGGER.debug("evaluating " + (beanClass + " class"));
        boolean ignore = isExcluded(beanClass);
        if (!ignore) {
            //no matching pattern have been found
            //we don't remove beanProperties
            return beanProperties;
        }
        LOGGER.debug("ignoring " + (beanClass + " class"));

        return Lists.newArrayList();
    }


    private boolean isExcluded(Class className) {
        for (Pattern pattern : filters) {
            Matcher matcher = pattern.matcher(className.getName());
            if (matcher.matches()) {
                return true;
            }
        }
        return false;
    }
}
