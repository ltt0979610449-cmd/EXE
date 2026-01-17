package swd.coiviet.dto.request;

import lombok.Data;

@Data
public class UpdateUserMemoryRequest {
    private String title;
    private String description;
    private Long provinceId;
}