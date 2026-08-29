package org.reciplease.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Lightweight test slice for asserting Jakarta Bean Validation constraints directly on a
 * plain DTO/model/request object, without paying for a Spring context. There's no built-in
 * Spring Boot {@code @ValidationTest} slice (unlike {@code @WebMvcTest}, {@code @DataMongoTest},
 * etc.) — this meta-annotates {@link ExtendWith} with {@link ValidationExtension}, which resolves
 * a shared {@link jakarta.validation.Validator} for any test method parameter of that type.
 * <p>
 * Use this to unit-test the constraint itself (e.g. "amount &lt;= 0 is rejected", "barcode must
 * match the pattern"). It intentionally does NOT prove a controller actually validates its
 * request body — a missing/wrong {@code @Valid} on a controller method is invisible here, since
 * validation runs directly against the object graph, not through MVC argument resolution. Pair
 * it with the existing {@code @WebMvcTest} controller tests, which prove the end-to-end
 * "invalid JSON body over HTTP → 400" behaviour.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(ValidationExtension.class)
public @interface ValidationTest {}
