package todolist.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import todolist.dto.UsuarioData;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class AdminRegistrationTest {

    @Autowired
    private UsuarioService usuarioService;

    @Test
    public void testRegistrarPrimerAdmin() {
        // Registrar primer admin
        UsuarioData admin = new UsuarioData();
        admin.setEmail("admin@umh.es");
        admin.setPassword("admin123");
        admin.setAdmin(true);

        usuarioService.registrar(admin);

        // Verificar que existe un admin
        assertThat(usuarioService.existsByAdmin(true)).isTrue();
    }

    @Test
    public void testRegistrarSegundoAdminFalla() {
        // Registrar primer admin
        UsuarioData admin1 = new UsuarioData();
        admin1.setEmail("admin1@umh.es");
        admin1.setPassword("admin123");
        admin1.setAdmin(true);
        usuarioService.registrar(admin1);

        // Intentar registrar segundo admin
        UsuarioData admin2 = new UsuarioData();
        admin2.setEmail("admin2@umh.es");
        admin2.setPassword("admin456");
        admin2.setAdmin(true);

        // Debe lanzar excepción
        assertThrows(UsuarioServiceException.class, () -> usuarioService.registrar(admin2));
    }
}