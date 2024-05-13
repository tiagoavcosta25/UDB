package org.socialnetwork.user;

import org.socialnetwork.annotations.DTO;

@DTO
public record UserDTO(String username, String password) {
}
