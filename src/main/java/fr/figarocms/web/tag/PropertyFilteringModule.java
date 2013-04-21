package fr.figarocms.web.tag;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.google.common.collect.ImmutableMultimap;

import java.util.regex.Pattern;

/**
 * code adapted from https://github.com/ultraflynn/jackson-custom-serialization .
 */
public final class PropertyFilteringModule extends Module {
    private static final Version MODULE_VERSION = new Version(1, 0, 0, null);
    private final String moduleName;
    private final ImmutableMultimap<Pattern, String> filters;

    public static Builder builder(String moduleName) {
        return new Builder(moduleName);
    }

    public static final class Builder {
        private final String moduleName;
        private final ImmutableMultimap.Builder<Pattern, String> filterBuilder = new ImmutableMultimap.Builder<>();

        private Builder(String moduleName) {
            this.moduleName = moduleName;
        }

        public Builder exclude(Pattern classPatternForExclusion, String name) {
            filterBuilder.put(classPatternForExclusion, name);
            return this;
        }

        public PropertyFilteringModule build() {
            return new PropertyFilteringModule(this);
        }
    }

    private PropertyFilteringModule(Builder builder) {
        moduleName = builder.moduleName;
        filters = builder.filterBuilder.build();
    }

    @Override
    public String getModuleName() {
        return moduleName;
    }

    @Override
    public Version version() {
        return MODULE_VERSION;
    }

    @Override
    public void setupModule(Module.SetupContext context) {
        context.addBeanSerializerModifier(
                FilteringBeanSerializerModifier.excluding(filters));
    }
}
