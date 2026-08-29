package org.reciplease.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;
import org.reciplease.model.PasskeyUserHandles;

class PasskeyUserEntityRepositoryImplTest {

    private final PasskeyUserEntityRepositoryImpl repository = new PasskeyUserEntityRepositoryImpl();

    @Test
    void findByUsernameDerivesAnEntityWhoseIdIsTheUsernamesUtf8Bytes() {
        final var entity = repository.findByUsername("user-1");

        assertThat(entity.getName(), is("user-1"));
        assertThat(entity.getDisplayName(), is("user-1"));
        assertThat(PasskeyUserHandles.toUserId(entity.getId()), is("user-1"));
    }

    @Test
    void findByIdDerivesTheSameEntityAsFindByUsername() {
        final var handle = PasskeyUserHandles.toHandle("user-1");

        final var entity = repository.findById(handle);

        assertThat(entity.getName(), is("user-1"));
        assertThat(entity.getId(), is(handle));
    }

    @Test
    void saveAndDeleteAreNoOpsBecauseTheresNothingToPersist() {
        final var entity = repository.findByUsername("user-1");

        repository.save(entity);
        repository.delete(entity.getId());

        // Still derivable purely from the username/id — nothing was actually stored either way.
        assertThat(repository.findByUsername("user-1").getName(), is("user-1"));
    }
}
