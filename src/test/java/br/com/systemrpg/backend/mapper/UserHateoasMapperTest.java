package br.com.systemrpg.backend.mapper;

import br.com.systemrpg.backend.dto.response.RoleResponse;
import br.com.systemrpg.backend.dto.response.UserResponse;
import br.com.systemrpg.backend.dto.hateoas.UserHateoasResponse;
import br.com.systemrpg.backend.hateoas.PageInfo;
import br.com.systemrpg.backend.hateoas.PagedHateoasResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a classe UserHateoasMapper.
 */
class UserHateoasMapperTest {

    private UserHateoasMapper userHateoasMapper;
    private UserResponse userResponse1;
    private UserResponse userResponse2;
    private RoleResponse roleResponse;

    @BeforeEach
    void setUp() {
        userHateoasMapper = Mappers.getMapper(UserHateoasMapper.class);
        
        // Setup test role response
        roleResponse = RoleResponse.builder()
                .id(UUID.randomUUID())
                .name("ROLE_USER")
                .description("User role")
                .build();
        
        // Setup test user responses
        userResponse1 = UserResponse.builder()
                .id(UUID.randomUUID())
                .username("user1")
                .email("user1@example.com")
                .firstName("User")
                .lastName("One")
                .isActive(true)
                .isEmailVerified(true)
                .roles(Set.of(roleResponse))
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .lastLoginAt(LocalDateTime.now().minusHours(1))
                .build();
                
        userResponse2 = UserResponse.builder()
                .id(UUID.randomUUID())
                .username("user2")
                .email("user2@example.com")
                .firstName("User")
                .lastName("Two")
                .isActive(false)
                .isEmailVerified(false)
                .roles(Set.of(roleResponse))
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusHours(2))
                .build();
    }

    @Test
    void toHateoasResponse_ShouldMapUserResponseToUserHateoasResponse() {
        // Act
        UserHateoasResponse result = userHateoasMapper.toHateoasResponse(userResponse1);

        // Assert
        assertNotNull(result);
        assertEquals(result.getId(), userResponse1.getId());
        assertEquals(result.getUsername(), userResponse1.getUsername());
        assertEquals(result.getEmail(), userResponse1.getEmail());
        assertEquals(result.getFirstName(), userResponse1.getFirstName());
        assertEquals(result.getLastName(), userResponse1.getLastName());
        assertEquals(result.getIsActive(), userResponse1.getIsActive());
        assertEquals(result.getIsEmailVerified(), userResponse1.getIsEmailVerified());
        // Convert Set to List for comparison since mapper might change collection type
        assertEquals(new ArrayList<>(userResponse1.getRoles()), new ArrayList<>(result.getRoles()));
        assertEquals(result.getCreatedAt(), userResponse1.getCreatedAt());
        assertEquals(result.getUpdatedAt(), userResponse1.getUpdatedAt());
        assertEquals(result.getLastLoginAt(), userResponse1.getLastLoginAt());
        
        // Verify links are initialized
        assertNotNull(result.getLinks());
        assertTrue(result.getLinks().isEmpty());
    }

    @Test
    void toHateoasResponse_WithNullUserResponse_ShouldReturnNull() {
        // Act
        UserHateoasResponse result = userHateoasMapper.toHateoasResponse(null);

        // Assert
        assertNull(result);
    }

    @Test
    void toHateoasResponse_WithNullRoles_ShouldHandleNullRoles() {
        // Arrange
        UserResponse userResponseWithNullRoles = UserResponse.builder()
                .id(UUID.randomUUID())
                .username("user3")
                .email("user3@example.com")
                .firstName("User")
                .lastName("Three")
                .isActive(true)
                .isEmailVerified(true)
                .roles(null) // This is the key test case
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .lastLoginAt(LocalDateTime.now().minusHours(1))
                .build();

        // Act
        UserHateoasResponse result = userHateoasMapper.toHateoasResponse(userResponseWithNullRoles);

        // Assert
        assertNotNull(result);
        assertEquals(result.getId(), userResponseWithNullRoles.getId());
        assertEquals(result.getUsername(), userResponseWithNullRoles.getUsername());
        assertEquals(result.getEmail(), userResponseWithNullRoles.getEmail());
        assertNull(result.getRoles()); // Should handle null roles gracefully
        
        // Verify links are initialized
        assertNotNull(result.getLinks());
        assertTrue(result.getLinks().isEmpty());
    }

    @Test
    void toHateoasResponseList_ShouldMapListOfUserResponses() {
        // Arrange
        List<UserResponse> userResponses = List.of(userResponse1, userResponse2);

        // Act
        List<UserHateoasResponse> result = userHateoasMapper.toHateoasResponseList(userResponses);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Verify first user
        UserHateoasResponse hateoasResponse1 = result.get(0);
        assertEquals(hateoasResponse1.getId(), userResponse1.getId());
        assertEquals(hateoasResponse1.getUsername(), userResponse1.getUsername());
        assertEquals(hateoasResponse1.getEmail(), userResponse1.getEmail());
        
        // Verify second user
        UserHateoasResponse hateoasResponse2 = result.get(1);
        assertEquals(hateoasResponse2.getId(), userResponse2.getId());
        assertEquals(hateoasResponse2.getUsername(), userResponse2.getUsername());
        assertEquals(hateoasResponse2.getEmail(), userResponse2.getEmail());
    }

    @Test
    void toHateoasResponseList_WithNullList_ShouldReturnNull() {
        // Act
        List<UserHateoasResponse> result = userHateoasMapper.toHateoasResponseList(null);

        // Assert
        assertNull(result);
    }

    @Test
    void toHateoasResponseList_WithEmptyList_ShouldReturnEmptyList() {
        // Act
        List<UserHateoasResponse> result = userHateoasMapper.toHateoasResponseList(List.of());

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void toPagedHateoasResponse_ShouldMapPageToPagedHateoasResponse() {
        // Arrange
        List<UserResponse> content = List.of(userResponse1, userResponse2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserResponse> page = new PageImpl<>(content, pageable, 2);

        // Act
        PagedHateoasResponse<UserHateoasResponse> result = userHateoasMapper.toPagedHateoasResponse(page);

        // Assert
        assertNotNull(result);
        
        // Verify content
        assertNotNull(result.getContent());
        assertEquals(2, result.getContent().size());
        assertEquals(result.getContent().get(0).getId(), userResponse1.getId());
        assertEquals(result.getContent().get(1).getId(), userResponse2.getId());
        
        // Verify page info
        PageInfo pageInfo = result.getPage();
        assertNotNull(pageInfo);
        assertEquals(0, pageInfo.getNumber());
        assertEquals(10, pageInfo.getSize());
        assertEquals(2L, pageInfo.getTotalElements());
        assertEquals(1, pageInfo.getTotalPages());
        assertTrue(pageInfo.isFirst());
        assertTrue(pageInfo.isLast());
        assertFalse(pageInfo.isHasNext());
        assertFalse(pageInfo.isHasPrevious());
        assertEquals(2, pageInfo.getNumberOfElements());
        
        // Verify links are initialized
        assertNotNull(result.getLinks());
        assertTrue(result.getLinks().isEmpty());
    }

    @Test
    void toPagedHateoasResponse_WithMultiplePages_ShouldSetCorrectPageInfo() {
        // Arrange
        List<UserResponse> content = List.of(userResponse1, userResponse2);
        Pageable pageable = PageRequest.of(1, 2); // Second page, 2 items per page
        Page<UserResponse> page = new PageImpl<>(content, pageable, 5); // Total 5 elements

        // Act
        PagedHateoasResponse<UserHateoasResponse> result = userHateoasMapper.toPagedHateoasResponse(page);

        // Assert
        assertNotNull(result);
        
        PageInfo pageInfo = result.getPage();
        assertNotNull(pageInfo);
        assertEquals(1, pageInfo.getNumber()); // Second page (0-indexed)
        assertEquals(2, pageInfo.getSize());
        assertEquals(5L, pageInfo.getTotalElements());
        assertEquals(3, pageInfo.getTotalPages()); // 5 elements / 2 per page = 3 pages
        assertFalse(pageInfo.isFirst());
        assertFalse(pageInfo.isLast());
        assertTrue(pageInfo.isHasNext());
        assertTrue(pageInfo.isHasPrevious());
        assertEquals(2, pageInfo.getNumberOfElements());
    }

    @Test
    void toPagedHateoasResponse_WithEmptyPage_ShouldHandleCorrectly() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserResponse> page = new PageImpl<>(List.of(), pageable, 0);

        // Act
        PagedHateoasResponse<UserHateoasResponse> result = userHateoasMapper.toPagedHateoasResponse(page);

        // Assert
        assertNotNull(result);
        
        // Verify content
        assertNotNull(result.getContent());
        assertTrue(result.getContent().isEmpty());
        
        // Verify page info
        PageInfo pageInfo = result.getPage();
        assertNotNull(pageInfo);
        assertEquals(0, pageInfo.getNumber());
        assertEquals(10, pageInfo.getSize());
        assertEquals(0L, pageInfo.getTotalElements());
        assertEquals(0, pageInfo.getTotalPages());
        assertTrue(pageInfo.isFirst());
        assertTrue(pageInfo.isLast());
        assertFalse(pageInfo.isHasNext());
        assertFalse(pageInfo.isHasPrevious());
        assertEquals(0, pageInfo.getNumberOfElements());
    }

    @Test
    void toPagedHateoasResponse_WithSinglePage_ShouldSetCorrectFlags() {
        // Arrange
        List<UserResponse> content = List.of(userResponse1);
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserResponse> page = new PageImpl<>(content, pageable, 1);

        // Act
        PagedHateoasResponse<UserHateoasResponse> result = userHateoasMapper.toPagedHateoasResponse(page);

        // Assert
        assertNotNull(result);
        
        PageInfo pageInfo = result.getPage();
        assertNotNull(pageInfo);
        assertTrue(pageInfo.isFirst());
        assertTrue(pageInfo.isLast());
        assertFalse(pageInfo.isHasNext());
        assertFalse(pageInfo.isHasPrevious());
        assertEquals(1, pageInfo.getTotalPages());
    }
}
