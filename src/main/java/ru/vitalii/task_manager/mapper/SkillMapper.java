package ru.vitalii.task_manager.mapper;

import org.springframework.stereotype.Component;
import ru.vitalii.task_manager.dto.SkillRequest;
import ru.vitalii.task_manager.dto.SkillResponse;
import ru.vitalii.task_manager.model.Skill;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SkillMapper {

    public SkillResponse toResponse(Skill skill) {
        if (skill == null) {
            return null;
        }
        return SkillResponse.builder()
                .id(skill.getId())
                .name(skill.getName())
                .description(skill.getDescription())
                .build();
    }

    public Skill toEntity(SkillRequest request) {
        if (request == null) {
            return null;
        }
        Skill skill = new Skill();
        skill.setName(request.getName());
        skill.setDescription(request.getDescription());
        return skill;
    }

    public Set<SkillResponse> toResponseSet(Set<Skill> skills) {
        if (skills == null) {
            return Set.of();
        }
        return skills.stream()
                .map(this::toResponse)
                .collect(Collectors.toSet());
    }
}