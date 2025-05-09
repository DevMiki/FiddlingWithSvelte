package tech.thepack.core.resource.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.thepack.core.resource.model.Attachment;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
}