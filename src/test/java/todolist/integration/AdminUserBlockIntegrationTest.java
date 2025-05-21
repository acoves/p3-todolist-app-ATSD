package todolist.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import todolist.model.Usuario;
import todolist.repository.UsuarioRepository;
import todolist.service.UsuarioService;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AdminUserBlockIntegrationTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void fullAdminBlockWorkflow_shouldPersistBlockedStatus() {
        // Create test user
        Usuario user = new Usuario("test@integration.com");
        user.setEnabled(true);
        usuarioRepository.save(user);

        // Block user
        usuarioService.toggleUserStatus(user.getId(), false);

        // Verify
        Usuario blockedUser = usuarioRepository.findById(user.getId()).get();
        assertFalse(blockedUser.isEnabled());

        // Unblock user
        usuarioService.toggleUserStatus(user.getId(), true);

        // Verify
        Usuario unblockedUser = usuarioRepository.findById(user.getId()).get();
        assertTrue(unblockedUser.isEnabled());
    }
}