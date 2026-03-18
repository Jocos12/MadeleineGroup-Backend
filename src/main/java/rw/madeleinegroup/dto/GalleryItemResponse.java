package rw.madeleinegroup.dto;

public class GalleryItemResponse {
    private Long id;
    private String imageUrl;
    private String alt;
    private String category;
    private Boolean isVideo;

    public GalleryItemResponse() {}
    public GalleryItemResponse(Long id, String imageUrl, String alt, String category, Boolean isVideo) {
        this.id = id; this.imageUrl = imageUrl; this.alt = alt; this.category = category; this.isVideo = isVideo;
    }
    public static GalleryItemResponseBuilder builder() { return new GalleryItemResponseBuilder(); }
    public static class GalleryItemResponseBuilder {
        private Long id; private String imageUrl, alt, category; private Boolean isVideo;
        public GalleryItemResponseBuilder id(Long v) { id = v; return this; } public GalleryItemResponseBuilder imageUrl(String v) { imageUrl = v; return this; } public GalleryItemResponseBuilder alt(String v) { alt = v; return this; } public GalleryItemResponseBuilder category(String v) { category = v; return this; } public GalleryItemResponseBuilder isVideo(Boolean v) { isVideo = v; return this; }
        public GalleryItemResponse build() { return new GalleryItemResponse(id, imageUrl, alt, category, isVideo); }
    }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; } public String getImageUrl() { return imageUrl; } public void setImageUrl(String v) { this.imageUrl = v; } public String getAlt() { return alt; } public void setAlt(String v) { this.alt = v; } public String getCategory() { return category; } public void setCategory(String v) { this.category = v; } public Boolean getIsVideo() { return isVideo; } public void setIsVideo(Boolean v) { this.isVideo = v; }
}
