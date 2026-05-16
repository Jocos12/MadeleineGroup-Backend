package rw.madeleinegroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rw.madeleinegroup.entity.Announcement;

import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findByActiveTrueOrderByCreatedAtDesc();

    @Query("SELECT DISTINCT a FROM Announcement a LEFT JOIN FETCH a.createdBy WHERE a.active = true ORDER BY a.createdAt DESC")
    List<Announcement> findActiveWithAuthors();

    @Query("SELECT DISTINCT a FROM Announcement a LEFT JOIN FETCH a.createdBy ORDER BY a.createdAt DESC")
    List<Announcement> findAllWithAuthors();
}
