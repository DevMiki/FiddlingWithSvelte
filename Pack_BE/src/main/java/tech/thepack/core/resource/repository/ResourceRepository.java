package tech.thepack.core.resource.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.thepack.core.resource.model.Resource;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

}
