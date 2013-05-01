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

        final Optional<HttpSession> session = Optional.fromNullable(pageContext.getSession());
        if (session.isPresent()) {
            attributeValue = session.get().getAttribute(attributeName.or(EMPTY_ATTRIBUTE_NAME));
        }
        return Optional.fromNullable(attributeValue);
    }
}
