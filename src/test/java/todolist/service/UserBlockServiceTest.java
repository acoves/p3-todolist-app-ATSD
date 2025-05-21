package todolist.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import todolist.model.Usuario;
import todolist.repository.UsuarioRepository;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserBlockServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void toggleUserStatus_shouldBlockUserWhenEnabledIsFalse() {
        // Given
        Usuario user = new Usuario("test@example.com");
        user.setId(1L);
        user.setEnabled(true);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        usuarioService.toggleUserStatus(1L, false);

        // Then
        assertFalse(user.isEnabled());
        verify(usuarioRepository).save(user);
    }

    @Test
    void toggleUserStatus_shouldUnblockUserWhenEnabledIsTrue() {
        // Given
        Usuario user = new Usuario("blocked@example.com");
        user.setId(2L);
        user.setEnabled(false);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(user));

        // When
        usuarioService.toggleUserStatus(2L, true);

        // Then
        assertTrue(user.isEnabled());
        verify(usuarioRepository).save(user);
    }

    @Test
    void toggleUserStatus_shouldThrowExceptionForNonExistingUser() {
        when(usuarioRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(UsuarioServiceException.class, () ->
                usuarioService.toggleUserStatus(99L, false)
        );
    }
}