package rw.madeleinegroup.dto;

import java.util.List;

public class BookingSearchResponse {
    private List<BookingResponse> content;
    private long totalCount;
    private int totalPages;
    private int currentPage;
    private int pageSize;

    public BookingSearchResponse() {}

    public BookingSearchResponse(List<BookingResponse> content, long totalCount, int totalPages, int currentPage, int pageSize) {
        this.content = content;
        this.totalCount = totalCount;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }

    public List<BookingResponse> getContent() { return content; }
    public void setContent(List<BookingResponse> content) { this.content = content; }
    public long getTotalCount() { return totalCount; }
    public void setTotalCount(long totalCount) { this.totalCount = totalCount; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
}
