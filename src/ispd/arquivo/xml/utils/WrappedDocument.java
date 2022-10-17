package ispd.arquivo.xml.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Utility class to add convenience methods to manipulate XML Document objects.
 * It functions as a wrapper, outsourcing method calls to the inner object.
 */
public class WrappedDocument {
    public final Document document;

    /**
     * Construct a wrapper to abstract calls to the {@link Document} passed in.
     *
     * @param doc {@link Document} to be wrapped
     * @throws NullPointerException if the parameter {@code doc} is {@code null}
     */
    public WrappedDocument(final Document doc) {
        Objects.requireNonNull(doc);
        this.document = doc;
    }

    /**
     * Creates an element with the specified tag in the inner {@link Document}.
     *
     * @param s Tag name of the element to be created
     * @return {@link Element} hosted within the inner {@link Document}
     */
    public Element createElement(final String s) {
        return this.document.createElement(s);
    }

    /**
     * Append a child {@link Node} to the inner {@link Document}.
     *
     * @param node child to be appended
     */
    public void appendChild(final Node node) {
        this.document.appendChild(node);
    }

    /**
     * @return {@code true} if the model in the inner document has no
     * elements with the tag "owner"
     */
    public boolean hasNoOwners() {
        return this.hasEmptyTag("owner");
    }

    private boolean hasEmptyTag(final String tag) {
        return this.document.getElementsByTagName(tag).getLength() == 0;
    }

    /**
     * @return {@code true} if the model in the inner document has no
     * elements with the tag "machine"
     */
    public boolean hasNoMachines() {
        return this.hasEmptyTag("machine");
    }

    /**
     * @return {@code true} if the model in the inner document has no
     * elements with the tag "machine", or no elements with such tag have an
     * inner tag with the attribute "master"
     */
    public boolean hasNoMasters() {
        return this.machines()
                .noneMatch(WrappedElement::hasMasterAttribute);
    }

    /**
     * @return elements in the model with the tag "machine"
     */
    public Stream<WrappedElement> machines() {
        return this.elementsWithTag("machine");
    }

    private Stream<WrappedElement> elementsWithTag(final String tag) {
        return WrappedElement.nodeListToWrappedElementStream(
                this.document.getElementsByTagName(tag)
        );
    }

    /**
     * @return {@code true} if the model in the inner document has no
     * elements with the tag "cluster"
     */
    public boolean hasNoClusters() {
        return this.hasEmptyTag("cluster");
    }

    /**
     * @return {@code true} if the model in the inner document has no
     * elements with the tag "load"
     */
    public boolean hasNoLoads() {
        return this.hasEmptyTag("load");
    }

    /**
     * @return elements in the model with the tag "owner"
     */
    public Stream<WrappedElement> owners() {
        return this.elementsWithTag("owner");
    }

    /**
     * @return elements in the model with the tag "machine" <b>and</b> with
     * an inner tag with the attribute "master"
     */
    public Stream<WrappedElement> masters() {
        return this.machines()
                .filter(WrappedElement::hasMasterAttribute);
    }

    /**
     * @return elements in the model with the tag "cluster"
     */
    public Stream<WrappedElement> clusters() {
        return this.elementsWithTag("cluster");
    }

    /**
     * @return elements in the model with the tag "internet"
     */
    public Stream<WrappedElement> internets() {
        return this.elementsWithTag("internet");
    }

    /**
     * @return elements in the model with the tag "link"
     */
    public Stream<WrappedElement> links() {
        return this.elementsWithTag("link");
    }

    /**
     * @return elements in the model with the tag "virtualMac"
     */
    public Stream<WrappedElement> virtualMachines() {
        return this.elementsWithTag("virtualMac");
    }

    /**
     * @return elements in the model with the tag "load"
     */
    public Stream<WrappedElement> loads() {
        return this.elementsWithTag("load");
    }

    /**
     * @return the first element in the model with the tag "ispd", or {@code
     * null} if none are present
     */
    public WrappedElement ispd() {
        return this.elementsWithTag("ispd")
                .findFirst()
                .orElse(null);
    }
}