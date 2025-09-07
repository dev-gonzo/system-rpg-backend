package br.com.systemrpg.backend.mapper;

import br.com.systemrpg.backend.dto.hateoas.GameGroupHateoasResponse;
import br.com.systemrpg.backend.dto.response.GameGroupResponse;
import br.com.systemrpg.backend.hateoas.PageInfo;
import br.com.systemrpg.backend.hateoas.PagedHateoasResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Mapper para conversão entre GameGroupResponse e GameGroupHateoasResponse.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface GameGroupHateoasMapper {
    
    /**
     * Converte GameGroupResponse para GameGroupHateoasResponse.
     */
    @Mapping(target = "links", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    GameGroupHateoasResponse toHateoasResponse(GameGroupResponse gameGroupResponse);
    
    /**
     * Converte lista de GameGroupResponse para lista de GameGroupHateoasResponse.
     */
    List<GameGroupHateoasResponse> toHateoasResponseList(List<GameGroupResponse> gameGroupResponses);

    /**
     * Converte Page<GameGroupResponse> para PagedHateoasResponse<GameGroupHateoasResponse>.
     */
    default PagedHateoasResponse<GameGroupHateoasResponse> toPagedHateoasResponse(Page<GameGroupResponse> page) {
        List<GameGroupHateoasResponse> content = toHateoasResponseList(page.getContent());
        
        PageInfo pageInfo = new PageInfo();
        pageInfo.setNumber(page.getNumber());
        pageInfo.setSize(page.getSize());
        pageInfo.setTotalElements(page.getTotalElements());
        pageInfo.setTotalPages(page.getTotalPages());
        pageInfo.setFirst(page.isFirst());
        pageInfo.setLast(page.isLast());
        pageInfo.setHasNext(page.hasNext());
        pageInfo.setHasPrevious(page.hasPrevious());
        pageInfo.setNumberOfElements(page.getNumberOfElements());
        
        return new PagedHateoasResponse<>(content, pageInfo);
    }

}