package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void shouldConvertToJson() throws Exception {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужен мощный перфоратор");

        String jsonString = json.write(requestDto).getJson();

        assertThat(jsonString).contains("\"description\":\"Нужен мощный перфоратор\"");
    }

    @Test
    void shouldConvertFromJson() throws Exception {
        String jsonString = "{\"description\":\"Нужен мощный перфоратор\"}";

        ItemRequestDto requestDto = json.parse(jsonString).getObject();

        assertThat(requestDto.getDescription()).isEqualTo("Нужен мощный перфоратор");
    }

    @Test
    void shouldWorkWithNullFields() throws Exception {
        ItemRequestDto requestDto = new ItemRequestDto();

        String jsonString = json.write(requestDto).getJson();

        assertThat(jsonString).contains("\"description\":null");
    }
}
