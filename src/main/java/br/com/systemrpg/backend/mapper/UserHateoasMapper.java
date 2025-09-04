package br.com.systemrpg.backend.mapper;

import br.com.systemrpg.backend.dto.UserResponse;
import br.com.systemrpg.backend.dto.hateoas.UserHateoasResponse;
import br.com.systemrpg.backend.hateoas.PageInfo;
import br.com.systemrpg.backend.hateoas.PagedHateoasResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Mapper para conversão entre UserResponse e UserHateoasResponse.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserHateoasMapper {
    
    /**
     * Converte UserResponse para UserHateoasResponse.
     */
    @Mapping(target = "links", ignore = true)
    @Mapping(target = "fullName", expression = "java(userResponse.getFirstName() + \" \" + userResponse.getLastName())")
    UserHateoasResponse toHateoasResponse(UserResponse userResponse);
    
    /**
     * Converte lista de UserResponse para lista de UserHateoasResponse.
     */
    List<UserHateoasResponse> toHateoasResponseList(List<UserResponse> userResponses);
    
    /**
     * Converte Page<UserResponse> para PagedHateoasResponse<UserHateoasResponse>.
     */
    default PagedHateoasResponse<UserHateoasResponse> toPagedHateoasResponse(Page<UserResponse> page) {
        List<UserHateoasResponse> content = toHateoasResponseList(page.getContent());
        
        PageInfo pageInfo = PageInfo.builder()
            .number(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .first(page.isFirst())
            .last(page.isLast())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .numberOfElements(page.getNumberOfElements())
            .build();
        
        return PagedHateoasResponse.<UserHateoasResponse>builder()
            .content(content)
            .page(pageInfo)
            .build();
    }
}
