package br.com.systemrpg.backend.hateoas;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe base para responses que incluem links HATEOAS.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class HateoasResponse {
    
    /**
     * Lista de links HATEOAS.
     */
    @JsonProperty("_links")
    @lombok.Builder.Default
    private List<Link> links = new ArrayList<>();
    
    /**
     * Adiciona um link à resposta.
     */
    public void addLink(Link link) {
        if (this.links == null) {
            this.links = new ArrayList<>();
        }
        this.links.add(link);
    }

    /**
     * Adiciona um link à resposta com href, rel e método.
     */
    public void addLink(String href, String rel, String method) {
        addLink(new Link(href, rel, method));
    }


}
