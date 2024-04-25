package org.example.clearsolutionstest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.example.clearsolutionstest.dto.CreateUserDto;
import org.example.clearsolutionstest.dto.GetUserDto;
import org.example.clearsolutionstest.dto.UpdateUserDto;
import org.example.clearsolutionstest.service.UserService;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(
            description = "Returns list of users by specified filter"
    )
    @ApiResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(
                            schema = @Schema(implementation = GetUserDto.class)
                    )
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Returns message containing all validation errors.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionResponse.class)
            )
    )
    public List<GetUserDto> getUsers(@RequestParam LocalDate from, @RequestParam LocalDate to) {
        List<GetUserDto> allByBirthDateRange = userService.findAllByBirthDateRange(from, to);
        allByBirthDateRange.forEach(user ->
                user.add(linkTo(methodOn(UserController.class).deleteUser(user.getId()))
                        .withRel("selfDelete").withType(HttpMethod.DELETE.toString()))
        );
        return allByBirthDateRange;
    }

    @PostMapping
    @Operation(
            description = "Create user."
    )
    @ApiResponse(
            responseCode = "201",
            description = "User was successfully created. It returns id of created user",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(pattern = "{\"id\": \"28db0f04-0678-486a-80fb-2c7465bf0e13\"}")
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Returns message containing all validation errors.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionResponse.class)
            )
    )
    public ResponseEntity<Map<String, UUID>> createUser(@RequestBody CreateUserDto userDto) {
        return ResponseEntity.status(HttpStatus.CREATED.value())
                .body(Map.of("id", userService.createUser(userDto)));
    }

    @PutMapping("/{id}")
    @Operation(
            description = "Updates user with given id with data from UpdateUserDto." +
                          " If UpdateUserDto has null fields then that specific field will be ignored"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Means that execution fact was updated and all given parameters were changed"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Either user with given id does not exist or given updateDto is not valid. " +
                          "Message will contain detailed information",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionResponse.class)
            )
    )
    public ResponseEntity<EntityModel<Void>> updateUser(@PathVariable UUID id, @RequestBody UpdateUserDto userDto) {
        userService.updateUser(id, userDto);
        return ResponseEntity.ok(new EntityModel<Void>() {}
                .add(
                        linkTo(methodOn(UserController.class).deleteUser(id))
                                .withRel("selfDelete").withType(HttpMethod.DELETE.toString())
                ));
    }

    @DeleteMapping("/{id}")
    @Operation(description = "Deletes user by specified id. If there is no such user with given id than it is ignored")
    @ApiResponse(
            responseCode = "200",
            description = "User with given id deleted."
    )
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

}
