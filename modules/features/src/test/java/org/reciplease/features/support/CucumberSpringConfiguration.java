package org.reciplease.features.support;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.data.mongodb.test.autoconfigure.AutoConfigureDataMongo;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

/**
 * Boots {@link FeaturesTestApplication} (core + web + database, component-scanned together)
 * against an embedded Mongo, with MockMvc as the HTTP entry point for Cucumber steps. Scenarios
 * drive the app through MockMvc rather than a real HTTP client — see
 * {@code org.reciplease.configuration.MethodSecurityTestSupport} in the {@code web} module's
 * tests for why that's the simpler path for exercising {@code @PreAuthorize}, applied here too.
 */
@CucumberContextConfiguration
@SpringBootTest(classes = FeaturesTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@AutoConfigureDataMongo
public class CucumberSpringConfiguration {}
