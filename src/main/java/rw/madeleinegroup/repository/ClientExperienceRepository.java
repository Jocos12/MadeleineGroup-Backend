package rw.madeleinegroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.madeleinegroup.entity.ClientExperience;

import java.util.List;

public interface ClientExperienceRepository extends JpaRepository<ClientExperience, Long> {

    List<ClientExperience> findByApprovalStatus(ClientExperience.ApprovalStatus status);
}
