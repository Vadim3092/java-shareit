package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService requestService;

    @Test
    void createRequest_ShouldReturnCreated() throws Exception {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужен мощный перфоратор");

        ItemRequestResponseDto responseDto = new ItemRequestResponseDto();
        responseDto.setId(1L);
        responseDto.setDescription("Нужен мощный перфоратор");
        responseDto.setCreated(LocalDateTime.now());

        when(requestService.create(eq(1L), any(ItemRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Нужен мощный перфоратор"))
                .andExpect(jsonPath("$.created").exists());
    }

    @Test
    void getUserRequests_ShouldReturnList() throws Exception {
        ItemRequestResponseDto request1 = new ItemRequestResponseDto();
        request1.setId(1L);
        request1.setDescription("Нужна дрель");

        ItemRequestResponseDto request2 = new ItemRequestResponseDto();
        request2.setId(2L);
        request2.setDescription("Нужен перфоратор");

        List<ItemRequestResponseDto> requests = Arrays.asList(request1, request2);

        when(requestService.getUserRequests(1L)).thenReturn(requests);

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("Нужна дрель"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].description").value("Нужен перфоратор"));
    }

    @Test
    void getAllRequests_WithDefaultParams_ShouldReturnList() throws Exception {
        ItemRequestResponseDto request1 = new ItemRequestResponseDto();
        request1.setId(1L);
        request1.setDescription("Чужой запрос 1");

        ItemRequestResponseDto request2 = new ItemRequestResponseDto();
        request2.setId(2L);
        request2.setDescription("Чужой запрос 2");

        List<ItemRequestResponseDto> requests = Arrays.asList(request1, request2);

        when(requestService.getAllRequests(eq(1L), eq(0), eq(10))).thenReturn(requests);

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void getAllRequests_WithCustomParams_ShouldReturnList() throws Exception {
        ItemRequestResponseDto request1 = new ItemRequestResponseDto();
        request1.setId(3L);
        request1.setDescription("Чужой запрос 3");

        List<ItemRequestResponseDto> requests = Arrays.asList(request1);

        when(requestService.getAllRequests(eq(1L), eq(2), eq(5))).thenReturn(requests);

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "2")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3));
    }

    @Test
    void getRequestById_ShouldReturnRequest() throws Exception {
        ItemRequestResponseDto responseDto = new ItemRequestResponseDto();
        responseDto.setId(1L);
        responseDto.setDescription("Нужна дрель");
        responseDto.setCreated(LocalDateTime.now());

        when(requestService.getRequestById(1L, 1L)).thenReturn(responseDto);

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Нужна дрель"));
    }
}
