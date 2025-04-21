package group.quankane.service;

import group.quankane.dto.request.UserRequestDTO;
import group.quankane.dto.response.PageResponse;
import group.quankane.dto.response.UserDetailResponse;
import group.quankane.model.User;
import group.quankane.util.UserStatus;
import jakarta.mail.MessagingException;
import org.springframework.data.domain.Pageable;

import java.io.UnsupportedEncodingException;
import java.util.List;

public interface UserService {

    long saveUser(UserRequestDTO request) throws MessagingException, UnsupportedEncodingException;

    void updateUser(long userId, UserRequestDTO request);

    void changeStatus(long userId, UserStatus status);

    void deleteUser(long userId);

    String confirmUser(int userId, String verifyCode);

    UserDetailResponse getUser(long userId);

    PageResponse<List<UserDetailResponse>> getAllUsersWithSortBy(int pageNo, int pageSize, String sortBy);

    PageResponse<List<UserDetailResponse>> getAllUsersWithSortByMultipleColumns(int pageNo, int pageSize, String... sorts);

    PageResponse<List<User>> getAllUsersAndSearchWithPagingAndSorting(int pageNo, int pageSize, String search, String sortBy);

    PageResponse<List<User>> advanceSearchWithCriteria(int pageNo, int pageSize, String sortBy, String address, String... search);

    PageResponse<Object> advanceSearchWithSpecifications(Pageable pageable, String[] user, String[] address);


}
