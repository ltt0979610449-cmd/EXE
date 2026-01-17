package swd.coiviet.dto.request;

import lombok.Data;

@Data
public class UpdateArtisanRequest {
    private String fullName;
    private String specialization;
    private String bio;
    private Long provinceId;
    private String workshopAddress;
}