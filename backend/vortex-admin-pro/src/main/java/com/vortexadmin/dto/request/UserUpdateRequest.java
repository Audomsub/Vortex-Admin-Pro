package com.vortexadmin.dto.request;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String firstName;
    private String lastName;
    private Long roleId;
    private String status;
}
