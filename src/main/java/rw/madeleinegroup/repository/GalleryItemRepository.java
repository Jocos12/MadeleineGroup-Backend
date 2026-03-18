package rw.madeleinegroup.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.madeleinegroup.entity.GalleryItem;

import java.util.List;

public interface GalleryItemRepository extends JpaRepository<GalleryItem, Long> {

    List<GalleryItem> findByCategory(String category);

    List<GalleryItem> findAllByOrderByCreatedAtDesc();

    List<GalleryItem> findByBranch_Id(Long branchId);
}
