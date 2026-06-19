package com.stationery.inventory;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InventoryServiceApplicationTest {

    @Test
    void main_invokesSpringApplicationRun() {
        try (MockedStatic<SpringApplication> springApp = mockStatic(SpringApplication.class)) {
            springApp.when(() -> SpringApplication.run(
                            any(Class.class), any(String[].class)))
                    .thenReturn(mock(ConfigurableApplicationContext.class));

            InventoryServiceApplication.main(new String[]{});

            springApp.verify(() -> SpringApplication.run(
                    InventoryServiceApplication.class, new String[]{}));
        }
    }
}