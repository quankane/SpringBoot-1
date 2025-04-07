package group.quankane.service.impl;

import group.quankane.dto.request.UserRequestDTO;
import group.quankane.exception.ResourceNotFoundException;
import group.quankane.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Override
    public int addUser(UserRequestDTO userRequestDTO) {
        System.out.println("Save user to db");
        if (!userRequestDTO.getFirstName().equals("Tay")) {
            throw new ResourceNotFoundException("Tay khong ton tai");
        }
        return 0;
    }
}
