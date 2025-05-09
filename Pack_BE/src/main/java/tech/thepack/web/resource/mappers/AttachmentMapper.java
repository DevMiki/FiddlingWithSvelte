package tech.thepack.web.resource.mappers;

import org.mapstruct.Mapper;
import tech.thepack.core.resource.model.Attachment;
import tech.thepack.web.resource.dto.AttachmentMetadataDTO;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AttachmentMapper {
    AttachmentMetadataDTO toMetadataDto(Attachment attachment);
    List<AttachmentMetadataDTO> toMetadataDtoList(List<Attachment> attachments);
}