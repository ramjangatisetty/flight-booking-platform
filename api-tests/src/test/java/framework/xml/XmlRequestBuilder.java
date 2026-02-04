package framework.xml;

/**
 * Abstract base class for building XML request payloads.
 */
public abstract class XmlRequestBuilder<T extends XmlRequestBuilder<T>> {

    protected String namespace;

    @SuppressWarnings("unchecked")
    public T withNamespace(String namespace) {
        this.namespace = namespace;
        return (T) this;
    }

    public abstract String build();

    protected String getNamespaceAttribute() {
        if (namespace != null && !namespace.isBlank()) {
            return " xmlns=\"" + namespace + "\"";
        }
        return "";
    }
}
