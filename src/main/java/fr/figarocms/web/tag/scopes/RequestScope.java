package fr.figarocms.web.tag.scopes;

import com.google.common.base.Optional;

import javax.servlet.jsp.PageContext;

class RequestScope extends Scope {

    public RequestScope() {
        this.scopeIdentifier = PageContext.REQUEST_SCOPE;
    }

    @Override
    public Optional<Object> getAttributeValue(PageContext pageContext, Optional<String> attributeName) {
        return Optional.fromNullable(pageContext.getRequest().getAttribute(attributeName.or(EMPTY_ATTRIBUTE_NAME)));
    }
}
