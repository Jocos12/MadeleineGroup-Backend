package rw.madeleinegroup.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.madeleinegroup.entity.Notification;
import rw.madeleinegroup.entity.User;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.read = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByUser(@Param("user") User user);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.user = :user")
    void markAllAsReadByUser(@Param("user") User user);

    long countByUserAndReadFalse(User user);

    List<Notification> findByReadFalseOrderByCreatedAtDesc();

    long countByReadFalse();

    @Query("SELECT n FROM Notification n WHERE (n.user IS NULL OR n.user = :user) ORDER BY n.createdAt DESC")
    List<Notification> findForUserOrderByCreatedAtDesc(@Param("user") User user, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE (n.user IS NULL OR n.user = :user) AND n.read = false")
    long countUnreadForUser(@Param("user") User user);

    List<Notification> findFirst50ByOrderByCreatedAtDesc();
}
