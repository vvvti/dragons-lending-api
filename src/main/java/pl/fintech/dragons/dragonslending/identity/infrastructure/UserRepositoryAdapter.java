package pl.fintech.dragons.dragonslending.identity.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.fintech.dragons.dragonslending.identity.domain.User;
import pl.fintech.dragons.dragonslending.identity.domain.UserPrincipal;
import pl.fintech.dragons.dragonslending.identity.domain.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository repository;

    @Override
    public User save(User user) {
        return repository.save(user);
    }

    @Override
    public User getOne(UUID id) {
        return repository.getOne(id);
    }

    @Override
    public List<User> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return repository.findByUsernameContainingIgnoreCase(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmailContainingIgnoreCase(email);
    }

    @Override
    public UserPrincipal getPrincipalFor(String email) {
        return repository.findByEmailContainingIgnoreCase(email)
                .map(user -> new UserPrincipal(user.getId(), user.getEmail(), user.getPassword()))
                .orElseThrow(() -> new UsernameNotFoundException("User with email:" + email + " doesn't exist in database"));
    }

    interface UserJpaRepository extends JpaRepository<User, UUID> {
        Optional<User> findByUsernameContainingIgnoreCase(String username);
        Optional<User> findByEmailContainingIgnoreCase(String email);
    }
}
