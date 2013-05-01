package fr.figarocms.web.tag.scopes;

import com.google.common.base.Optional;

import javax.servlet.jsp.PageContext;

class PageScope extends Scope {

    public PageScope() {
        this.scopeIdentifier = PageContext.PAGE_SCOPE;
    }

    @Override
    public Optional<Object> getAttributeValue(PageContext pageContext, Optional<String> attributeName) {
        return Optional.fromNullable(pageContext.getAttribute(attributeName.or(EMPTY_ATTRIBUTE_NAME)));
    }
}
