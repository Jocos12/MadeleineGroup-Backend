package rw.madeleinegroup.dto;

import jakarta.validation.constraints.NotBlank;

public class GalleryItemRequest {
    @NotBlank
    private String imageUrl;
    @NotBlank
    private String alt;
    private String category;
    private Boolean isVideo;

    public GalleryItemRequest() {}
    public GalleryItemRequest(String imageUrl, String alt, String category, Boolean isVideo) {
        this.imageUrl = imageUrl; this.alt = alt; this.category = category; this.isVideo = isVideo;
    }
    public static GalleryItemRequestBuilder builder() { return new GalleryItemRequestBuilder(); }
    public static class GalleryItemRequestBuilder {
        private String imageUrl, alt, category; private Boolean isVideo;
        public GalleryItemRequestBuilder imageUrl(String v) { imageUrl = v; return this; } public GalleryItemRequestBuilder alt(String v) { alt = v; return this; } public GalleryItemRequestBuilder category(String v) { category = v; return this; } public GalleryItemRequestBuilder isVideo(Boolean v) { isVideo = v; return this; }
        public GalleryItemRequest build() { return new GalleryItemRequest(imageUrl, alt, category, isVideo); }
    }
    public String getImageUrl() { return imageUrl; } public void setImageUrl(String v) { this.imageUrl = v; } public String getAlt() { return alt; } public void setAlt(String v) { this.alt = v; } public String getCategory() { return category; } public void setCategory(String v) { this.category = v; } public Boolean getIsVideo() { return isVideo; } public void setIsVideo(Boolean v) { this.isVideo = v; }
}
