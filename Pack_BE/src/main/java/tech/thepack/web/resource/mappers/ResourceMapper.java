package tech.thepack.web.resource.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import tech.thepack.core.resource.model.Resource;
import tech.thepack.web.resource.dto.ResourceDTO;
import tech.thepack.web.resource.dto.ResourceFormDataDTO;

import java.util.List;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ResourceMapper {

    List<ResourceDTO> toDtoList(List<Resource> resources);

    @Mapping(target = "attachmentCount", expression = "java(resource.getAttachments() != null ? resource.getAttachments().size() : 0)")
    ResourceDTO toDto(Resource resource);

    Resource toEntity(ResourceFormDataDTO dto);
}