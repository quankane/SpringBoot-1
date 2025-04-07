package group.quankane.service;

import group.quankane.dto.request.UserRequestDTO;

public interface UserService {
    int addUser(UserRequestDTO userRequestDTO);
}
