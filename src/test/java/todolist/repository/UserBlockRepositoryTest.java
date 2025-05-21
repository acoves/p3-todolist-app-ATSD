package todolist.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import todolist.model.Usuario;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserBlockRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void findByEnabled_shouldReturnOnlyActiveUsersWhenTrue() {
        // Given
        Usuario activeUser = new Usuario("active@test.com");
        activeUser.setEnabled(true);
        usuarioRepository.save(activeUser);

        Usuario blockedUser = new Usuario("blocked@test.com");
        blockedUser.setEnabled(false);
        usuarioRepository.save(blockedUser);

        // When
        List<Usuario> result = usuarioRepository.findByEnabled(true);

        // Then
        assertEquals(1, result.size());
        assertEquals("active@test.com", result.get(0).getEmail());
    }

    @Test
    void findByEnabled_shouldReturnOnlyBlockedUsersWhenFalse() {
        // Given
        Usuario blockedUser = new Usuario("blocked@test.com");
        blockedUser.setEnabled(false);
        usuarioRepository.save(blockedUser);

        // When
        List<Usuario> result = usuarioRepository.findByEnabled(false);

        // Then
        assertEquals(1, result.size());
        assertEquals("blocked@test.com", result.get(0).getEmail());
    }
}