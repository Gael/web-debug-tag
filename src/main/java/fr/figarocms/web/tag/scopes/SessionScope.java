package fr.figarocms.web.tag.scopes;

import com.google.common.base.Optional;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

class SessionScope extends Scope {


    public SessionScope() {
        this.scopeIdentifier = PageContext.SESSION_SCOPE;
    }

    @Override
    public Optional<Object> getAttributeValue(PageContext pageContext, Optional<String> attributeName) {
        Object attributeValue = null;

        final HttpSession session = pageContext.getSession();
        if (session != null) {
            attributeValue = session.getAttribute(attributeName.or(EMPTY_ATTRIBUTE_NAME));
        }
        return Optional.fromNullable(attributeValue);
    }
}
