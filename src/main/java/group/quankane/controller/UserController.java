package group.quankane.controller;

import group.quankane.dto.request.UserRequestDTO;
import group.quankane.dto.response.ResponseData;
import group.quankane.dto.response.ResponseError;
import group.quankane.dto.response.ResponseSuccess;
import group.quankane.service.UserService;
import group.quankane.util.Gender;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Constraint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/user")
@Validated
public class UserController {
    @Autowired
    private UserService userService;
//    Dùng kiểu Responses thông thường bằng code
//    @Operation(summary = "summary", description = "description", responses = {
//            @ApiResponse(responseCode = "201", description = "User added successfully",
//                        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
//                                examples = @ExampleObject(name = "ex name", summary = "ex summary",
//                                value = """
//                                        {
//                                            "status": 201,
//                                            "message": "User added successfully",
//                                            "data": 1
//                                        }
//                                        """
//                                ))
//
//            )
//    })
    @PostMapping(value = "/")
    public ResponseData<?> addUser(@Valid @RequestBody UserRequestDTO userRequestDTO) {
        System.out.println("Request add user " + userRequestDTO.getFirstName());
        try {
            userService.addUser(userRequestDTO);
            return new ResponseData<>(HttpStatus.CREATED.value(), "User added successfully", 1);
        } catch (Exception e) {
            return new ResponseError(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }
    }
    @PutMapping("/{userId}")
    public ResponseData<?> updateUser(@Min(1) @PathVariable int userId, @Valid @RequestBody UserRequestDTO userRequestDTO) {
        System.out.println("Request update userId=" + userId);
        return new ResponseData<>(HttpStatus.ACCEPTED.value(), "User updated successfully");
    }

    @PatchMapping("/{userId}")
    public ResponseData<?> changeStatus(@Min(1) @PathVariable int userId,@Valid @RequestParam boolean status) {
        System.out.println("Request change user status, userId = " + userId);
        return new ResponseData<>(HttpStatus.ACCEPTED.value(), "User status changed");
    }

    @DeleteMapping("/{userId}")
    public ResponseData<?> deleteUser(@PathVariable @Min(value = 1, message = "userId must be greater than 0") int userId) {
        System.out.println("Request deleted userId = " + userId);
        return new ResponseData<>(HttpStatus.NO_CONTENT.value(), "User deleted");
    }

    @GetMapping("/{userId}")
    public ResponseData<?> getUser(@PathVariable @Min(1) int userId) {
        System.out.println("Request get user detail by userId = " + userId);
        return new ResponseData<>(HttpStatus.OK.value(), "User", new UserRequestDTO("Quân", "Kane", "quanducbui2017@gmail.com","0822091972" ));
    }

    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    public ResponseData<List<UserRequestDTO>> getAllUser(
            @Min(0) @RequestParam(defaultValue = "0", required = false) int pageNo,
            @Min(10) @RequestParam(defaultValue = "10", required = false) int pageSize) {
        System.out.println("Request get all user");
        return new ResponseData<List<UserRequestDTO>>(HttpStatus.OK.value(), "List user",List.of(new UserRequestDTO("Quân", "Kane", "quanducbui2017@gmail.com","0822091972" ),
                new UserRequestDTO("Quân", "Kane", "quanducbui2017@gmail.com","0822091972" )));

    }
}
