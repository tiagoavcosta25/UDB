package org.socialnetwork.user.dto;

import org.framework.annotations.DTO;

@DTO
public record UserDTO(String username, String password) {
}
