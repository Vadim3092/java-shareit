package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserDtoJsonTest {

    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    void shouldConvertToJson() throws Exception {
        UserDto user = new UserDto();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john@test.com");

        String jsonString = json.write(user).getJson();

        assertThat(jsonString).contains("\"id\":1");
        assertThat(jsonString).contains("\"name\":\"John Doe\"");
        assertThat(jsonString).contains("\"email\":\"john@test.com\"");
    }

    @Test
    void shouldConvertFromJson() throws Exception {
        String jsonString = "{\"id\":1,\"name\":\"John Doe\",\"email\":\"john@test.com\"}";

        UserDto user = json.parse(jsonString).getObject();


        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("John Doe");
        assertThat(user.getEmail()).isEqualTo("john@test.com");
    }

    @Test
    void shouldWorkWithNullFields() throws Exception {
        UserDto user = new UserDto();
        user.setId(1L);

        String jsonString = json.write(user).getJson();


        assertThat(jsonString).contains("\"id\":1");
        assertThat(jsonString).contains("\"name\":null");
        assertThat(jsonString).contains("\"email\":null");
    }
}
