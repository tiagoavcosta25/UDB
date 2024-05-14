package org.socialnetwork.user;

import org.socialnetwork.annotations.DTO;
import org.socialnetwork.annotations.NotBlank;

@DTO
public record UserDTO(@NotBlank String username, @NotBlank String password) {
}
