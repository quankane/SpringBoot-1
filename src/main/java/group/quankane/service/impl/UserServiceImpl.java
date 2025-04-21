package group.quankane.service.impl;

import group.quankane.repository.specification.UserSpecificationBuilder;
import group.quankane.service.MailService;
import group.quankane.util.AppConstants;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import group.quankane.dto.request.AddressDTO;
import group.quankane.dto.request.UserRequestDTO;
import group.quankane.dto.response.PageResponse;
import group.quankane.dto.response.UserDetailResponse;
import group.quankane.exception.ResourceNotFoundException;
import group.quankane.model.Address;
import group.quankane.model.User;
import group.quankane.repository.SearchRepository;
import group.quankane.repository.UserRepository;
import group.quankane.service.UserService;
import group.quankane.util.UserStatus;
import group.quankane.util.UserType;

import java.beans.Transient;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static group.quankane.util.AppConstants.SORT_BY;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SearchRepository searchRepository;
    private final MailService mailService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    @Transactional(rollbackOn = Exception.class)
    public long saveUser(UserRequestDTO request) throws MessagingException, UnsupportedEncodingException {
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .phone(request.getPhone())
                .email(request.getEmail())
                .username(request.getUsername())
                .password(request.getPassword())
                .status(request.getStatus())
                .type(UserType.valueOf(request.getType().toUpperCase()))
                .build();
        request.getAddresses().forEach(a ->
                user.saveAddress(Address.builder()
                        .apartmentNumber(a.getApartmentNumber())
                        .floor(a.getFloor())
                        .building(a.getBuilding())
                        .streetNumber(a.getStreetNumber())
                        .street(a.getStreet())
                        .city(a.getCity())
                        .country(a.getCountry())
                        .addressType(Integer.valueOf(a.getAddressType()))
                        .build()));

        User result = userRepository.save(user);

        log.info("User has saved!");

        if (result != null) {
//            mailService.sendConfirmLink(user.getEmail(), user.getId(), "code@123");
            kafkaTemplate.send("confirm-account-topic", String.format("email=%s,id=%s,code=%s", user.getEmail(), user.getId(), "code@123"));
        }

        return user.getId();
    }

    @Override
    public void updateUser(long userId, UserRequestDTO request) {
        User user = getUserById(userId);
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setGender(request.getGender());
        user.setPhone(request.getPhone());
        if (!request.getEmail().equals(user.getEmail())) {
            // check email from database if not exist then allow update email otherwise throw exception
            user.setEmail(request.getEmail());
        }
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setStatus(request.getStatus());
        user.setType(UserType.valueOf(request.getType().toUpperCase()));
        user.setAddresses(convertToAddress(request.getAddresses()));
        userRepository.save(user);

        log.info("User updated successfully");
    }

    @Override
    public void changeStatus(long userId, UserStatus status) {
        User user = getUserById(userId);
        user.setStatus(status);
        userRepository.save(user);
        log.info("status changed");
    }

    @Override
    public void deleteUser(long userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public String confirmUser(int userId, String verifyCode) {
        return "Confirmed";
    }

    @Override
    public UserDetailResponse getUser(long userId) {
        User user = getUserById(userId);
        return UserDetailResponse.builder()
                .id(userId)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .build();
    }

    @Override
    public PageResponse<List<UserDetailResponse>> getAllUsersWithSortBy(int pageNo, int pageSize, String sortBy) {
        int page = 0;
        if (pageNo > 0) {
            page = pageNo - 1;
        }

        List<Sort.Order> sorts = new ArrayList<>();

        if (StringUtils.hasLength(sortBy)) {
            // firstName:asc|desc
            Pattern pattern = Pattern.compile(SORT_BY);
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                if (matcher.group(3).equalsIgnoreCase("asc")) {
                    sorts.add(new Sort.Order(Sort.Direction.ASC, matcher.group(1)));
                } else {
                    sorts.add(new Sort.Order(Sort.Direction.DESC, matcher.group(1)));
                }
            }
        }

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(sorts));

        Page<User> users = userRepository.findAll(pageable);
        List<UserDetailResponse> response = users.stream().map(user -> UserDetailResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build()).toList();
        return PageResponse.<List<UserDetailResponse>>builder()
                .page(pageNo)
                .size(pageSize)
                .total(users.getTotalPages())
                .items(response)
                .build();
    }

    @Override
    public PageResponse<List<UserDetailResponse>> getAllUsersWithSortByMultipleColumns(int pageNo, int pageSize, String... sorts) {
        int page = 0;
        if (pageNo > 0) {
            page = pageNo - 1;
        }

        List<Sort.Order> orders = new ArrayList<>();

        if (sorts != null) {
            for (String sortBy : sorts) {
                log.info("sortBy: {}", sortBy);
                // firstName:asc|desc
                Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)");
                Matcher matcher = pattern.matcher(sortBy);
                if (matcher.find()) {
                    if (matcher.group(3).equalsIgnoreCase("asc")) {
                        orders.add(new Sort.Order(Sort.Direction.ASC, matcher.group(1)));
                    } else {
                        orders.add(new Sort.Order(Sort.Direction.DESC, matcher.group(1)));
                    }
                }
            }
        }

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(orders));

        Page<User> users = userRepository.findAll(pageable);
        List<UserDetailResponse> response = users.stream().map(user -> UserDetailResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build()).toList();
        return PageResponse.<List<UserDetailResponse>>builder()
                .page(pageNo)
                .size(pageSize)
                .total(users.getTotalPages())
                .items(response)
                .build();
    }

    @Override
    public PageResponse<List<User>> getAllUsersAndSearchWithPagingAndSorting(int pageNo, int pageSize, String search, String sort) {
        return searchRepository.searchUser(pageNo, pageSize, search, sort);
    }

    @Override
    public PageResponse<List<User>> advanceSearchWithCriteria(int pageNo, int pageSize, String sortBy, String address, String... search) {
        return searchRepository.searchUserByCriteria(pageNo, pageSize, sortBy, address, search);
    }

    @Override
    public PageResponse<Object> advanceSearchWithSpecifications(Pageable pageable, String[] user, String[] address) {
        if (user != null && address != null) {
            return searchRepository.searchUserByCriteriaWithJoin(pageable, user, address);
        } else if (user != null) {
            UserSpecificationBuilder builder = new UserSpecificationBuilder();

            Pattern pattern = Pattern.compile(AppConstants.SEARCH_SPEC_OPERATOR);
            for (String s : user) {
                Matcher matcher = pattern.matcher(s);
                if (matcher.find()) {
                    builder.with(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                }
            }

            Page<User> users = userRepository.findAll(Objects.requireNonNull(builder.build()), pageable);
            return convertToPageResponse(users, pageable);
        }
        return convertToPageResponse(userRepository.findAll(pageable), pageable);

    }

    private Set<Address> convertToAddress(Set<AddressDTO> addresses) {
        Set<Address> result = new HashSet<>();
        addresses.forEach(a ->
                result.add(Address.builder()
                        .apartmentNumber(a.getApartmentNumber())
                        .floor(a.getFloor())
                        .building(a.getBuilding())
                        .streetNumber(a.getStreetNumber())
                        .street(a.getStreet())
                        .city(a.getCity())
                        .country(a.getCountry())
                        .addressType(Integer.valueOf(a.getAddressType()))
                        .build())
        );
        return result;
    }

    private User getUserById(long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /**
     * Convert Page<User> to PageResponse
     *
     * @param users
     * @param pageable
     * @return
     */
    private PageResponse<Object> convertToPageResponse(Page<User> users, Pageable pageable) {
        List<UserDetailResponse> responses = users.stream().map(user -> UserDetailResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build()).toList();
        return PageResponse.builder()
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .total(users.getTotalPages())
                .items(responses)
                .build();
    }
}
