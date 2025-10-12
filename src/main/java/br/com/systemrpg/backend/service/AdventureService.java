package br.com.systemrpg.backend.service;

import br.com.systemrpg.backend.domain.entity.Adventure;
import br.com.systemrpg.backend.dto.request.AdventureUpdateRequest;
import br.com.systemrpg.backend.repository.AdventureRepository;
import br.com.systemrpg.backend.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdventureService {

    private final AdventureRepository adventureRepository;
    private final GameGroupService gameGroupService;
    private final MessageUtil messageUtil;

    @Transactional(readOnly = true)
    public Adventure findById(UUID id) {
        return adventureRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> new IllegalArgumentException(messageUtil.getMessage("service.adventure.not.found")));
    }

    @Transactional
    public Adventure createAdventure(Adventure adventure, UUID createdByUserId) {
        adventure.setCreatedBy(createdByUserId);
        return adventureRepository.save(adventure);
    }

    @Transactional(readOnly = true)
    public List<Adventure> listByGameGroupId(UUID gameGroupId) {
        return adventureRepository.findByGameGroup_IdAndDeletedAtIsNullOrderByCreatedAtDesc(gameGroupId);
    }

    @Transactional(readOnly = true)
    public Page<Adventure> listByGameGroupId(UUID gameGroupId, Pageable pageable) {
        return adventureRepository.findByGameGroup_IdAndDeletedAtIsNull(gameGroupId, pageable);
    }

    @Transactional
    public Adventure updateAdventure(UUID id, AdventureUpdateRequest request) {
        Adventure adventure = findById(id);
        adventure.setTitle(request.getTitle());
        adventure.setDescription(request.getDescription());
        adventure.setUpdatedAt(LocalDateTime.now());
        return adventureRepository.save(adventure);
    }

    @Transactional
    public void deleteAdventure(UUID id) {
        Adventure adventure = findById(id);
        adventure.setIsActive(false);
        adventure.setDeletedAt(LocalDateTime.now());
        adventureRepository.save(adventure);
    }

    @Transactional(readOnly = true)
    public boolean canManageAdventure(UUID adventureId, String username) {
        Adventure adventure = adventureRepository.findByIdAndDeletedAtIsNull(adventureId).orElse(null);
        if (adventure == null || adventure.getGameGroup() == null) {
            return false;
        }
        return gameGroupService.isGroupOwner(adventure.getGameGroup().getId(), username);
    }

    @Transactional(readOnly = true)
    public boolean canViewAdventure(UUID adventureId, String username) {
        Adventure adventure = adventureRepository.findByIdAndDeletedAtIsNull(adventureId).orElse(null);
        if (adventure == null || adventure.getGameGroup() == null) {
            return false;
        }
        return gameGroupService.canViewGroup(adventure.getGameGroup().getId(), username);
    }
}