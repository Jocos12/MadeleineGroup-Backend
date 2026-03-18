package rw.madeleinegroup.dto;

public class BranchResponse {
    private Long id;
    private String code;
    private String name;
    private String description;

    public BranchResponse() {}
    public BranchResponse(Long id, String code, String name, String description) {
        this.id = id; this.code = code; this.name = name; this.description = description;
    }
    public static BranchResponseBuilder builder() { return new BranchResponseBuilder(); }
    public static class BranchResponseBuilder {
        private Long id; private String code; private String name; private String description;
        public BranchResponseBuilder id(Long id) { this.id = id; return this; }
        public BranchResponseBuilder code(String code) { this.code = code; return this; }
        public BranchResponseBuilder name(String name) { this.name = name; return this; }
        public BranchResponseBuilder description(String description) { this.description = description; return this; }
        public BranchResponse build() { return new BranchResponse(id, code, name, description); }
    }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getCode() { return code; } public void setCode(String code) { this.code = code; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
}
