package br.com.systemrpg.backend.hateoas;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa um link HATEOAS.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Link {
    

    private String href;
    private String rel;
    private String method;
    private String type;
    private String title;

    public Link(String hrefValue, String relValue) {
        this.href = hrefValue;
        this.rel = relValue;
    }

    public Link(String hrefValue, String relValue, String methodValue) {
        this.href = hrefValue;
        this.rel = relValue;
        this.method = methodValue;
    }
}
