package br.com.systemrpg.backend.hateoas;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Response paginada com suporte a HATEOAS.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PagedHateoasResponse<T> extends HateoasResponse {
    

    private List<T> content;

    @JsonProperty("page")
    private PageInfo page;
    
    public PagedHateoasResponse(List<T> content, PageInfo page) {
        this.content = content;
        this.page = page;
    }

    // Getters manuais para resolver problemas do Lombok
    public List<T> getContent() {
        return content;
    }
    
    public PageInfo getPage() {
        return page;
    }
    
    // Setters manuais para resolver problemas do Lombok
    public void setContent(List<T> content) {
        this.content = content;
    }
    
    public void setPage(PageInfo page) {
        this.page = page;
    }

}
