package ru.vitalii.task_manager.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import ru.vitalii.task_manager.model.enums.ProjectStatus;

@Converter(autoApply = true)
public class ProjectStatusConverter implements AttributeConverter<ProjectStatus, String> {
    @Override
    public String convertToDatabaseColumn(ProjectStatus status) {
        return status == null ? null : status.name().toLowerCase();
    }
    @Override
    public ProjectStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ProjectStatus.valueOf(dbData.toUpperCase());
    }
}
