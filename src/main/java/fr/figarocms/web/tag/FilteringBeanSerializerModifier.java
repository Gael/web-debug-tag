package fr.figarocms.web.tag;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * code adapted from https://github.com/ultraflynn/jackson-custom-serialization .
 */
public final class FilteringBeanSerializerModifier
        extends BeanSerializerModifier {
    private final ImmutableMultimap<Pattern, String> filters;

    static FilteringBeanSerializerModifier excluding(ImmutableMultimap<Pattern, String> filters) {
        return new FilteringBeanSerializerModifier(filters);
    }

    private FilteringBeanSerializerModifier(ImmutableMultimap<Pattern, String> filters) {
        this.filters = filters;
    }

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                     BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
        if (filters == null || filters.size() == 0) return beanProperties;

        ImmutableCollection<String> filter = getFilterMatchingClassToExclude(beanDesc);
        if (filter == null) return beanProperties;

        List<BeanPropertyWriter> included = Lists.newArrayList();
        for (BeanPropertyWriter property : beanProperties)
            if (!filter.contains(property.getName()))
                included.add(property);

        return included;
    }

    private ImmutableCollection<String> getFilterMatchingClassToExclude(BeanDescription beanDesc) {
        for (Pattern pattern : filters.keySet()) {
            String className = beanDesc.getBeanClass().getName();
            Matcher matcher = pattern.matcher(className);
            if(matcher.matches()){
                return filters.get(pattern);
            }
        }
        return null;
    }
}
