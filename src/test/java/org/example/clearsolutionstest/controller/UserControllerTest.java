package org.example.clearsolutionstest.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolationException;
import lombok.SneakyThrows;
import org.example.clearsolutionstest.dto.CreateUserDto;
import org.example.clearsolutionstest.dto.GetUserDto;
import org.example.clearsolutionstest.dto.UpdateUserDto;
import org.example.clearsolutionstest.service.TimeService;
import org.example.clearsolutionstest.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    UserService userService;

    @SpyBean
    ModelMapper modelMapper;

    @SpyBean
    ObjectMapper objectMapper;

    @MockBean
    TimeService timeService;

    List<GetUserDto> expectedUsers = List.of(
            new GetUserDto(UUID.randomUUID(), "email1@gmail.com", "first 1", "last 1",
                    LocalDate.of(2000, 1, 1), "Country 1, City 1", "3803424234242"),
            new GetUserDto(UUID.randomUUID(), "email2@gmail.com", "first 2", "last 2",
                    LocalDate.of(2000, 2, 1), "Country 2, City 2", "3801243425253")
    );

    @BeforeEach
    void resetMocks() {
        reset(userService, modelMapper, objectMapper, timeService);
    }

    @SneakyThrows
    @Test
    void getUsers_shouldPassSameValuesToServiceAndShouldReturnSameValuesFromIt() {
        //given
        LocalDate from = LocalDate.of(2000, 1, 1);
        LocalDate to = LocalDate.of(2000, 5, 1);
        Integer pageIndex = 0;
        Integer pageSize = 50;
        given(userService.findAllByBirthDateRange(from, to, pageIndex, pageSize)).willReturn(expectedUsers);
        //when
        ResultActions result = mvc.perform(get("/api/v1/users")
                .param("from", formattedDate(from))
                .param("to", formattedDate(to))
                .param("pageIndex", pageIndex.toString())
                .param("pageSize", pageSize.toString()));
        //then
        String resultJson = result
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(expectedUsers.size())))
                .andExpect(jsonPath("[0].links[0].rel", is("selfDelete")))
                .andExpect(jsonPath(
                        "[0].links[0].href",
                        containsString("/api/v1/users/%s".formatted(expectedUsers.get(0).getId()))
                ))
                .andExpect(jsonPath("[0].links[0].type", is("DELETE")))
                .andExpect(jsonPath("[1].links[0].rel", is("selfDelete")))
                .andExpect(jsonPath(
                        "[1].links[0].href",
                        containsString("/api/v1/users/%s".formatted(expectedUsers.get(1).getId()))
                ))
                .andExpect(jsonPath("[1].links[0].type", is("DELETE")))
                .andReturn().getResponse().getContentAsString();
        List<GetUserDto> actualUsers = objectMapper.readValue(resultJson, new TypeReference<List<GetUserDto>>() {
        });
        assertTrue(expectedUsers.containsAll(actualUsers), "Result should contain all users.");
        verify(userService, times(1)).findAllByBirthDateRange(from, to, pageIndex, pageSize);
    }

    private String formattedDate(LocalDate date) {
        return date.format(DateTimeFormatter.ISO_DATE);
    }

    @SneakyThrows
    @Test
    void getUsers_shouldReturnExceptionResponse_ifIllegalArgumentExceptionWasThrownInService() {
        //given
        LocalDate from = LocalDate.of(2000, 6, 1);
        LocalDate to = LocalDate.of(2000, 5, 1);
        Integer pageIndex = 0;
        Integer pageSize = 50;
        String expectedMessage = "From date is after to date";
        given(userService.findAllByBirthDateRange(from, to, pageIndex, pageSize)).willThrow(new IllegalArgumentException(expectedMessage));
        LocalDateTime expectedExceptionTime = LocalDateTime.of(2001, 1, 1, 0, 0);
        given(timeService.utcNow()).willReturn(expectedExceptionTime);
        //when
        ResultActions result = mvc.perform(get("/api/v1/users")
                .param("from", formattedDate(from))
                .param("to", formattedDate(to))
                .param("pageIndex", pageIndex.toString())
                .param("pageSize", pageSize.toString()));
        //then
        String resultJson = result
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();
        ExceptionResponse actualResult = objectMapper.readValue(resultJson, ExceptionResponse.class);
        assertTrue(actualResult.getMessage().contains(expectedMessage), "Result should contain expected message.");
        assertEquals(expectedExceptionTime, actualResult.getDate());
        assertEquals(BAD_REQUEST, actualResult.getHttpStatus());
        verify(timeService, times(1)).utcNow();
        verify(userService, times(1)).findAllByBirthDateRange(from, to, pageIndex, pageSize);
    }

    @SneakyThrows
    @Test
    void getUsers_shouldAssignDefaultValuesForPageIndexAndPageSize_ifParametersAreNotSpecified() {
        //given
        LocalDate from = LocalDate.of(2000, 1, 1);
        LocalDate to = LocalDate.of(2000, 5, 1);
        Integer defaultPageIndex = 0;
        Integer defaultPageSize = 50;
        given(userService.findAllByBirthDateRange(from, to, defaultPageIndex, defaultPageSize)).willReturn(expectedUsers);
        //when
        ResultActions result = mvc.perform(get("/api/v1/users")
                .param("from", formattedDate(from))
                .param("to", formattedDate(to)));
        //then
        String resultJson = result
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(expectedUsers.size())))
                .andExpect(jsonPath("[0].links[0].rel", is("selfDelete")))
                .andExpect(jsonPath(
                        "[0].links[0].href",
                        containsString("/api/v1/users/%s".formatted(expectedUsers.get(0).getId()))
                ))
                .andExpect(jsonPath("[0].links[0].type", is("DELETE")))
                .andExpect(jsonPath("[1].links[0].rel", is("selfDelete")))
                .andExpect(jsonPath(
                        "[1].links[0].href",
                        containsString("/api/v1/users/%s".formatted(expectedUsers.get(1).getId()))
                ))
                .andExpect(jsonPath("[1].links[0].type", is("DELETE")))
                .andReturn().getResponse().getContentAsString();
        List<GetUserDto> actualUsers = objectMapper.readValue(resultJson, new TypeReference<List<GetUserDto>>() {
        });
        assertTrue(expectedUsers.containsAll(actualUsers), "Result should contain all users.");
        verify(userService, times(1)).findAllByBirthDateRange(from, to, defaultPageIndex, defaultPageSize);
    }

    @SneakyThrows
    @Test
    void createUser_shouldPassSameValuesToServiceAndReturnValueFromIt() {
        //given
        CreateUserDto createUserDto = new CreateUserDto(
                "email@gmail.com",
                String.join("", Collections.nCopies(100, "f")),
                String.join("", Collections.nCopies(100, "l")),
                LocalDate.of(2000, 1, 1),
                "Ukraine, Lviv",
                "380123123131"
        );
        UUID expectedId = UUID.randomUUID();
        given(userService.createUser(createUserDto)).willReturn(expectedId);
        //when
        ResultActions result = mvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDto)));
        //then
        String resultJson = result.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();
        UUID actualId = UUID.fromString(objectMapper.readValue(resultJson, Map.class).get("id").toString());
        assertEquals(expectedId, actualId);
        verify(userService, times(1)).createUser(createUserDto);
    }

    @SneakyThrows
    @Test
    void createUser_shouldReturnExceptionResponse_ifConstraintViolationExceptionWasThrownInService() {
        //given
        CreateUserDto createUserDto = new CreateUserDto(
                "emailgmail.com",
                String.join("", Collections.nCopies(101, "f")),
                String.join("", Collections.nCopies(101, "l")),
                LocalDate.of(2000, 1, 1),
                "Ukraine, Lviv",
                "380123123131"
        );
        LocalDateTime expectedExceptionTime = LocalDateTime.of(2001, 1, 1, 0, 0);
        given(timeService.utcNow()).willReturn(expectedExceptionTime);
        String expectedMessage = "Some message";
        given(userService.createUser(createUserDto)).willThrow(new ConstraintViolationException(expectedMessage, Set.of()));
        //when
        ResultActions result = mvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDto)));
        //then
        String resultJson = result
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();
        ExceptionResponse actualResult = objectMapper.readValue(resultJson, ExceptionResponse.class);
        assertTrue(actualResult.getMessage().contains(expectedMessage), "Result should contain expected message.");
        assertEquals(expectedExceptionTime, actualResult.getDate());
        assertEquals(BAD_REQUEST, actualResult.getHttpStatus());
        verify(timeService, times(1)).utcNow();
        verify(userService, times(1)).createUser(createUserDto);
    }

    @SneakyThrows
    @Test
    void updateUser_shouldPassSameValuesToService() {
        //given
        UpdateUserDto updateUserDto = new UpdateUserDto(
                "email@gmail.com",
                String.join("", Collections.nCopies(100, "f")),
                String.join("", Collections.nCopies(100, "l")),
                LocalDate.of(2000, 1, 1),
                "Ukraine, Lviv",
                "380123123131"
        );
        UUID id = UUID.randomUUID();
        //when
        ResultActions result = mvc.perform(put("/api/v1/users/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserDto)));
        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath(
                        "_links.selfDelete.href",
                        containsString("/api/v1/users/%s".formatted(id))
                ))
                .andExpect(jsonPath("_links.selfDelete.type", is("DELETE")));
        verify(userService, times(1)).updateUser(id, updateUserDto);
    }

    @SneakyThrows
    @Test
    void updateUser_shouldReturnExceptionResponse_ifConstraintViolationExceptionWasThrownInService() {
        //given
        UpdateUserDto updateUserDto = new UpdateUserDto(
                "email@gmail.com",
                String.join("", Collections.nCopies(100, "f")),
                String.join("", Collections.nCopies(100, "l")),
                LocalDate.of(2000, 1, 1),
                "Ukraine, Lviv",
                "380123123131"
        );
        UUID id = UUID.randomUUID();
        LocalDateTime expectedExceptionTime = LocalDateTime.of(2001, 1, 1, 0, 0);
        given(timeService.utcNow()).willReturn(expectedExceptionTime);
        String expectedMessage = "Some message";
        doThrow(new ConstraintViolationException(expectedMessage, Set.of())).when(userService).updateUser(id, updateUserDto);
        //when
        ResultActions result = mvc.perform(put("/api/v1/users/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserDto)));
        //then
        String resultJson = result
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();
        ExceptionResponse actualResult = objectMapper.readValue(resultJson, ExceptionResponse.class);
        assertTrue(actualResult.getMessage().contains(expectedMessage), "Result should contain expected message.");
        assertEquals(expectedExceptionTime, actualResult.getDate());
        assertEquals(BAD_REQUEST, actualResult.getHttpStatus());
        verify(timeService, times(1)).utcNow();
        verify(userService, times(1)).updateUser(id, updateUserDto);
    }

    @SneakyThrows
    @Test
    void updateUser_shouldReturnExceptionResponse_ifIllegalArgumentsExceptionWasThrownInService() {
        //given
        UpdateUserDto updateUserDto = new UpdateUserDto(
                "email@gmail.com",
                String.join("", Collections.nCopies(100, "f")),
                String.join("", Collections.nCopies(100, "l")),
                LocalDate.of(2000, 1, 1),
                "Ukraine, Lviv",
                "380123123131"
        );
        UUID id = UUID.randomUUID();
        LocalDateTime expectedExceptionTime = LocalDateTime.of(2001, 1, 1, 0, 0);
        given(timeService.utcNow()).willReturn(expectedExceptionTime);
        String expectedMessage = "Some message";
        doThrow(new IllegalArgumentException(expectedMessage)).when(userService).updateUser(id, updateUserDto);
        //when
        ResultActions result = mvc.perform(put("/api/v1/users/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateUserDto)));
        //then
        String resultJson = result
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse().getContentAsString();
        ExceptionResponse actualResult = objectMapper.readValue(resultJson, ExceptionResponse.class);
        assertTrue(actualResult.getMessage().contains(expectedMessage), "Result should contain expected message.");
        assertEquals(expectedExceptionTime, actualResult.getDate());
        assertEquals(BAD_REQUEST, actualResult.getHttpStatus());
        verify(timeService, times(1)).utcNow();
        verify(userService, times(1)).updateUser(id, updateUserDto);
    }

    @SneakyThrows
    @Test
    void deleteUser_shouldPassSameIdToService() {
        //given
        UUID id = UUID.randomUUID();
        //when
        ResultActions result = mvc.perform(delete("/api/v1/users/{id}", id));
        //then
        result.andExpect(status().isOk());
        verify(userService, times(1)).deleteUser(id);
    }

}
