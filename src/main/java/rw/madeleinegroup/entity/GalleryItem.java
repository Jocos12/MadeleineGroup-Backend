package rw.madeleinegroup.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "gallery_items")
public class GalleryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl;

    private String altText;

    @Column(nullable = false)
    private String category;

    @Column(name = "is_video")
    private Boolean isVideo = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public GalleryItem() {
    }

    public GalleryItem(Long id, String imageUrl, String altText, String category, Boolean isVideo,
                      Branch branch, LocalDateTime createdAt) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.altText = altText;
        this.category = category;
        this.isVideo = isVideo != null ? isVideo : false;
        this.branch = branch;
        this.createdAt = createdAt;
    }

    public static GalleryItemBuilder builder() {
        return new GalleryItemBuilder();
    }

    public static class GalleryItemBuilder {
        private Long id;
        private String imageUrl;
        private String altText;
        private String category;
        private Boolean isVideo = false;
        private Branch branch;
        private LocalDateTime createdAt;

        public GalleryItemBuilder id(Long id) { this.id = id; return this; }
        public GalleryItemBuilder imageUrl(String imageUrl) { this.imageUrl = imageUrl; return this; }
        public GalleryItemBuilder altText(String altText) { this.altText = altText; return this; }
        public GalleryItemBuilder category(String category) { this.category = category; return this; }
        public GalleryItemBuilder isVideo(Boolean isVideo) { this.isVideo = isVideo; return this; }
        public GalleryItemBuilder branch(Branch branch) { this.branch = branch; return this; }
        public GalleryItemBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public GalleryItem build() {
            return new GalleryItem(id, imageUrl, altText, category, isVideo, branch, createdAt);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getAltText() { return altText; }
    public void setAltText(String altText) { this.altText = altText; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Boolean getIsVideo() { return isVideo; }
    public void setIsVideo(Boolean isVideo) { this.isVideo = isVideo; }
    public Branch getBranch() { return branch; }
    public void setBranch(Branch branch) { this.branch = branch; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
