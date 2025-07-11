package todolist.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import todolist.service.UsuarioService;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import todolist.dto.UsuarioData;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminLoginTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioService usuarioService;
    // Test para verificar que un usuario administrador es redirigido a la lista de usuarios
    @Test
    public void testLoginAdminRedirigeAListaUsuarios() throws Exception {
        UsuarioData admin = new UsuarioData();
        admin.setEmail("admin@umh.es");
        admin.setPassword("admin123");
        admin.setAdmin(true);
        usuarioService.registrar(admin);

        mockMvc.perform(post("/login")
                        .param("eMail", "admin@umh.es")
                        .param("password", "admin123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/registered"));
    }
    // Test para verificar que un usuario normal es redirigido a la página de tareas
    @Test
    public void testLoginUsuarioNormalRedirigeATareas() throws Exception {
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@umh.es");
        usuario.setPassword("user123");
        usuarioService.registrar(usuario);

        mockMvc.perform(post("/login")
                        .param("eMail", "user@umh.es")
                        .param("password", "user123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/usuarios/*/tareas"));
    }
}