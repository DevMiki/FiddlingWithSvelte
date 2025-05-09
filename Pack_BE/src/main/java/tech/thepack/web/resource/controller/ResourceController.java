package tech.thepack.web.resource.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.thepack.core.resource.model.Attachment;
import tech.thepack.web.resource.dto.AttachmentMetadataDTO;
import tech.thepack.web.resource.dto.ResourceDTO;
import tech.thepack.web.resource.dto.ResourceFormDataDTO;
import tech.thepack.web.resource.facade.ResourceFacade;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("api/v1/resources")
public class ResourceController {

    private final ResourceFacade facade;

    public ResourceController(ResourceFacade facade) {
        this.facade = facade;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResourceDTO> save(
            @RequestPart("data") @Valid ResourceFormDataDTO resourceFormDataDTO,
            @RequestPart("files") List<MultipartFile> files) {
        final ResourceDTO resource = this.facade.save(resourceFormDataDTO, files);
        return ResponseEntity
                .created(URI.create("/api/v1/resources/%s".formatted(resource.getId())))
                .body(resource);
    }

    @GetMapping
    public ResponseEntity<List<ResourceDTO>> listAll() {
        return ResponseEntity.ok(this.facade.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourceDTO> getResourceById(@PathVariable("id") long id) {
        final ResourceDTO resourceDTO = facade.findByIdAndConvertToDto(id);
        return ResponseEntity.ok(resourceDTO);
    }

    @GetMapping("/{resourceId}/attachments")
    public ResponseEntity<List<AttachmentMetadataDTO>> listAttachmentsForResource(@PathVariable("resourceId") Long resourceId) {
        final List<AttachmentMetadataDTO> attachments = facade.getAttachmentsMetadata(resourceId);
        return ResponseEntity.ok(attachments);
    }

    @GetMapping("/attachments/{attachmentId}/download")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable("attachmentId") Long attachmentId) {
        final Attachment attachment = this.facade.getAttachmentFile(attachmentId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                .body(attachment.getFileData());
    }

    @GetMapping("/attachments/{attachmentId}/view")
    public ResponseEntity<byte[]> viewAttachment(@PathVariable("attachmentId") Long attachmentId) {
        final Attachment attachment = this.facade.getAttachmentFile(attachmentId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + attachment.getFileName() + "\"")
                .body(attachment.getFileData());
    }

}