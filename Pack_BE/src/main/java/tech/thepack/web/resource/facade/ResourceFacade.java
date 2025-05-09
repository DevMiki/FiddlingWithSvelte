package tech.thepack.web.resource.facade;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tech.thepack.core.resource.model.Attachment;
import tech.thepack.core.resource.model.Resource;
import tech.thepack.web.resource.dto.AttachmentMetadataDTO;
import tech.thepack.web.resource.dto.ResourceFormDataDTO;
import tech.thepack.web.resource.mappers.ResourceMapper;
import tech.thepack.web.resource.service.ResourceService;
import tech.thepack.web.resource.dto.ResourceDTO;

import java.util.List;

@Transactional
@Service
public class ResourceFacade {
    private final ResourceService resourceService;
    private final ResourceMapper resourceMapper;

    public ResourceFacade(ResourceService resourceService, ResourceMapper resourceMapper) {
        this.resourceService = resourceService;
        this.resourceMapper = resourceMapper;
    }

    public ResourceDTO save(ResourceFormDataDTO resourceFormDataDTO, List<MultipartFile> files) {
        return resourceService.save(resourceFormDataDTO, files);
    }

    public List<ResourceDTO> listAll(){
        return resourceService.listAll();
    }

    public ResourceDTO findByIdAndConvertToDto(long id) {
        return resourceMapper.toDto(resourceService.findById(id));
    }

    public Attachment getAttachmentFile(Long attachmentId) {
        return this.resourceService.getAttachmentFile(attachmentId);
    }

    public List<AttachmentMetadataDTO> getAttachmentsMetadata(Long resourceId) {
        return this.resourceService.getAttachmentsMetadata(resourceId);
    }
}
