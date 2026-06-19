package com.stationery.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Pure unit tests for {@link AuthServiceApplication}.
 * <p>
 * com.stationery.auth (the root package) contains only this bootstrap
 * class, so its JaCoCo PACKAGE-level coverage is entirely driven by how
 * much of this file executes. Rather than using {@code @SpringBootTest}
 * (which would start Eureka discovery, the Config Server client, the
 * database, and every other auto-configured bean just to cover one
 * line), {@link SpringApplication#run(Class, String...)} is mocked
 * statically so {@code main()} executes for real without ever building
 * an actual application context.
 */
@DisplayName("AuthServiceApplication Unit Tests")
class AuthServiceApplicationTest {

    @Test
    @DisplayName("main() delegates to SpringApplication.run with this class and the given args")
    void mainDelegatesToSpringApplicationRun() {
        String[] args = {"--server.port=0"};
        ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);

        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            springApplication.when(() -> SpringApplication.run(AuthServiceApplication.class, args))
                    .thenReturn(mockContext);

            AuthServiceApplication.main(args);

            springApplication.verify(
                    () -> SpringApplication.run(AuthServiceApplication.class, args),
                    times(1)
            );
        }
    }

    @Test
    @DisplayName("main() works correctly with no arguments")
    void mainWorksWithNoArguments() {
        String[] args = {};
        ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);

        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            springApplication.when(() -> SpringApplication.run(AuthServiceApplication.class, args))
                    .thenReturn(mockContext);

            AuthServiceApplication.main(args);

            springApplication.verify(
                    () -> SpringApplication.run(AuthServiceApplication.class, args),
                    times(1)
            );
        }
    }

    @Test
    @DisplayName("AuthServiceApplication can be instantiated")
    void applicationCanBeInstantiated() {
        AuthServiceApplication app = new AuthServiceApplication();

        assertNotNull(app);
    }
}