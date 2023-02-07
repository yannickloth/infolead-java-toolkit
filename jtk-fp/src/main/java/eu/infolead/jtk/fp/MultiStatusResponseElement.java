package eu.infolead.jtk.fp;

import java.net.URI;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface MultiStatusResponseElement {
    /**
     * The mandatory Hypertext reference.
     * 
     * @return the URI representing the Hypertext reference of the resource for
     *         which a status message is provided.
     */
    @JsonProperty("href")
    URI getHypertextReference();
}
