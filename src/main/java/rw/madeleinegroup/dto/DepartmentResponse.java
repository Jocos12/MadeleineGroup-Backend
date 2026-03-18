package rw.madeleinegroup.dto;

import java.util.List;

public class DepartmentResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private List<PackageItemResponse> packages;

    public DepartmentResponse() {}

    public DepartmentResponse(Long id, String code, String name, String description, List<PackageItemResponse> packages) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.packages = packages;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<PackageItemResponse> getPackages() { return packages; }
    public void setPackages(List<PackageItemResponse> packages) { this.packages = packages; }
}
