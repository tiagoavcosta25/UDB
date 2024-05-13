package org.socialnetwork.user;

import org.framework.annotations.DTO;

@DTO
public record UserDTO(String username, String password) {
}
