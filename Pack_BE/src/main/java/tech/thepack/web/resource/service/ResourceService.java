package tech.thepack.web.resource.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import tech.thepack.core.resource.config.FileUploadProperties;
import tech.thepack.core.resource.exception.AttachmentStorageException;
import tech.thepack.core.resource.exception.FileSizeLimitExceededException;
import tech.thepack.core.resource.exception.InvalidInputException;
import tech.thepack.core.resource.exception.ResourceNotFoundException;
import tech.thepack.core.resource.model.Attachment;
import tech.thepack.core.resource.model.Resource;
import tech.thepack.core.resource.repository.AttachmentRepository;
import tech.thepack.core.resource.repository.ResourceRepository;
import tech.thepack.web.resource.dto.AttachmentMetadataDTO;
import tech.thepack.web.resource.dto.ResourceDTO;
import tech.thepack.web.resource.dto.ResourceFormDataDTO;
import tech.thepack.web.resource.mappers.AttachmentMapper;
import tech.thepack.web.resource.mappers.ResourceMapper;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Transactional
@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final AttachmentRepository attachmentRepository;
    private final ResourceMapper resourceMapper;
    private final AttachmentMapper attachmentMapper;
    private final FileUploadProperties fileUploadProperties;

    public ResourceService(ResourceRepository resourceRepository,
                           AttachmentRepository attachmentRepository,
                           ResourceMapper resourceMapper,
                           AttachmentMapper attachmentMapper,
                           FileUploadProperties fileUploadProperties) {
        this.resourceRepository = resourceRepository;
        this.attachmentRepository = attachmentRepository;
        this.resourceMapper = resourceMapper;
        this.attachmentMapper = attachmentMapper;
        this.fileUploadProperties = fileUploadProperties;
    }

    public ResourceDTO save(ResourceFormDataDTO resourceFormDataDTO, List<MultipartFile> files) {
        if (files == null || files.isEmpty() || files.stream().allMatch(MultipartFile::isEmpty)) {
            throw new InvalidInputException("At least one file must be provided.");
        }

        final Resource resourceEntity = resourceMapper.toEntity(resourceFormDataDTO);

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            if (file.getSize() > fileUploadProperties.getMaxFileSize().toBytes()) {
                throw new FileSizeLimitExceededException(
                        "File " + file.getOriginalFilename() + " size exceeds the limit of " + fileUploadProperties.getStringMaxSizeInMB()
                );
            }
            try {
                final Attachment attachment = new Attachment();
                attachment.setFileName(StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename())));
                attachment.setFileType(file.getContentType());
                attachment.setFileSize(file.getSize());
                attachment.setFileData(file.getBytes());
                resourceEntity.addAttachment(attachment);
            } catch (IOException e) {
                throw new AttachmentStorageException("Could not store file " + file.getOriginalFilename() + ". Please try again!", e);
            }
        }

        final Resource savedEntity = resourceRepository.save(resourceEntity);
        return resourceMapper.toDto(savedEntity);
    }

    public List<ResourceDTO> listAll() {
        return resourceMapper.toDtoList(resourceRepository.findAll());
    }

    public Resource findById(long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource with id " + id + " not found."));
    }

    public Attachment getAttachmentFile(Long attachmentId) {
        final Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment with id " + attachmentId + " not found."));
        if (attachment.getFileData() == null || attachment.getFileData().length == 0) {
            throw new ResourceNotFoundException("Attachment with id " + attachmentId + " is incomplete or has no data.");
        }
        return attachment;
    }

    public List<AttachmentMetadataDTO> getAttachmentsMetadata(Long resourceId) {
        final Resource resource = findById(resourceId);
        return attachmentMapper.toMetadataDtoList(resource.getAttachments());
    }
}