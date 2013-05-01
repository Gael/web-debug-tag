package fr.figarocms.web.tag.scopes;

import com.google.common.base.Optional;

import javax.servlet.jsp.PageContext;

public abstract class Scope {

    public static final String EMPTY_ATTRIBUTE_NAME = "";
    protected Integer scopeIdentifier;

    public Integer getScopeIdentifier() {
        return scopeIdentifier;
    }

    public abstract Optional<Object> getAttributeValue(PageContext pageContext, Optional<String> attributeName);
}
