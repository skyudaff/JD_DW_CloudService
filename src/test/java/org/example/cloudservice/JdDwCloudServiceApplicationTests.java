package org.example.cloudservice;

import org.example.cloudservice.dto.TokenDto;
import org.example.cloudservice.dto.UserDto;
import org.example.cloudservice.entity.UserEntity;
import org.example.cloudservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@AutoConfigureMockMvc
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JdDwCloudServiceApplicationTests {
    @LocalServerPort
    private int port;
    @Autowired
    TestRestTemplate restTemplate;
    @Autowired
    UserRepository userRepository;

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres")
            .withExposedPorts(5432);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }


    @Test
    void testPostgresContainer() throws SQLException {
        String jdbcUrl = postgres.getJdbcUrl();
        String username = postgres.getUsername();
        String password = postgres.getPassword();

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            assertTrue(connection.isValid(1), "Connection is valid");
        }
    }

    @Test
    void testUserRepository() {
        UserEntity expectedUser = new UserEntity();
        expectedUser.setLogin("test@example.org");
        expectedUser.setPassword("test");
        userRepository.save(expectedUser);

        Optional<UserEntity> actualUserOptional = userRepository.findUserEntityByLogin("test@example.org");

        assertTrue(actualUserOptional.isPresent(), "Пользователь не найден");

        UserEntity actualUser = actualUserOptional.get();

        assertEquals(expectedUser.getLogin(), actualUser.getLogin(), "Логин не соответствует ожидаемому значению");
        assertEquals(expectedUser.getPassword(), actualUser.getPassword(), "Пароль не соответствует ожидаемому значению");
    }

    @Test
    void testLoginEndpoint() {
        UserDto userDto = UserDto.builder().login("user@example.org").password("user").build();

        TokenDto tokenDto = restTemplate.postForObject("http://localhost:" + port + "/login", userDto, TokenDto.class);

        assertNotNull(tokenDto);
        assertNotNull(tokenDto.authToken());
        System.out.println(tokenDto);
    }
}