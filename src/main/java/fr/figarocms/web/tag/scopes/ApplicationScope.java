package fr.figarocms.web.tag.scopes;

import com.google.common.base.Optional;

import javax.servlet.jsp.PageContext;

class ApplicationScope extends Scope {


    public ApplicationScope() {
        this.scopeIdentifier = PageContext.APPLICATION_SCOPE;
    }

    @Override
    public Optional<Object> getAttributeValue(PageContext pageContext, Optional<String> attributeName) {
        return Optional.fromNullable(pageContext.getServletContext().getAttribute(attributeName.or(EMPTY_ATTRIBUTE_NAME)));
    }
}
