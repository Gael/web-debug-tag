package fr.figarocms.web.tag.scopes;

public enum Scopes {
    APPLICATION(new ApplicationScope()),
    SESSION(new SessionScope()),
    REQUEST(new RequestScope()),
    PAGE(new PageScope());

    private Scope scope;
    private String key = this.name().toLowerCase();

    private Scopes(Scope scope) {
        this.scope = scope;
    }

    public Scope getScope() {
        return scope;
    }

    public String getKey() {
        return key;
    }


}
