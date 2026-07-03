package org.project.carsharingapp.config;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@TestConfiguration
public class TestClockConfig {
    public static final ZoneId TEST_ZONE = ZoneId.of("Europe/Warsaw");

    public static final LocalDate FIXED_NOW = LocalDate.of(2026, 6, 1);

    public static final Clock FIXED_CLOCK = Clock.fixed(
        FIXED_NOW.atStartOfDay(TEST_ZONE).toInstant(),
        TEST_ZONE
    );

    @Bean
    @Primary
    Clock fixedClock() {
        return FIXED_CLOCK;
    }

    @Bean
    @Primary
    public LocalValidatorFactoryBean validator() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();

        validator.setConfigurationInitializer(
            configuration -> configuration.clockProvider(() -> FIXED_CLOCK)
        );

        return validator;
    }
}
