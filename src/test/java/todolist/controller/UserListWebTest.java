package todolist.controller;

import todolist.authentication.ManagerUserSession;
import todolist.dto.UsuarioData;
import todolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Arrays;
import java.util.Collections;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
public class UserListWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    // =====================
    //        TESTS
    // =====================

    @Test
    public void testUserListRequiresAuthentication() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        mockMvc.perform(get("/registered"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void testUserListShowsDataWhenAuthenticated() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);

        // Mock usuario autenticado
        UsuarioData loggedUser = new UsuarioData();
        loggedUser.setId(1L);
        loggedUser.setNombre("Richard Stallman");
        loggedUser.setEmail("richard"); // Email sin dominio

        when(usuarioService.findById(1L)).thenReturn(loggedUser);

        // Mock segundo usuario
        UsuarioData usuario2 = new UsuarioData();
        usuario2.setId(2L);
        usuario2.setEmail("linus");

        when(usuarioService.findAllUsuarios()).thenReturn(Arrays.asList(loggedUser, usuario2));

        mockMvc.perform(get("/registered"))
                .andDo(print()) // Muestra el HTML en consola
                .andExpect(status().isOk())
                .andExpect(xpath("//td[text()='richard']").exists());

        mockMvc.perform(get("/registered"))
                .andDo(print()) // Depuración del HTML generado
                .andExpect(status().isOk())
                .andExpect(xpath("/html/body//table/tbody/tr[1]/td[2][text()='richard']").exists())
                .andExpect(xpath("/html/body//table/tbody/tr[2]/td[2][text()='linus']").exists());
    }

    @Test
    public void testUserListDoesNotShowPasswords() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);

        UsuarioData loggedUser = new UsuarioData();
        loggedUser.setId(1L);
        when(usuarioService.findById(1L)).thenReturn(loggedUser);

        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("test@umh.es");
        usuario.setPassword("1234");

        when(usuarioService.findAllUsuarios()).thenReturn(Collections.singletonList(usuario));

        mockMvc.perform(get("/registered"))
                .andExpect(content().string(not(containsString("1234"))));
    }

    @Test
    public void testUserListShowsEmptyMessage() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);

        UsuarioData loggedUser = new UsuarioData();
        loggedUser.setId(1L);
        when(usuarioService.findById(1L)).thenReturn(loggedUser);

        when(usuarioService.findAllUsuarios()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/registered"))
                .andExpect(content().string(containsString("No hay usuarios registrados")));
    }

    @Test
    public void testUserListShowsIdsCorrectly() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);

        UsuarioData loggedUser = new UsuarioData();
        loggedUser.setId(1L);
        when(usuarioService.findById(1L)).thenReturn(loggedUser);

        UsuarioData usuario = new UsuarioData();
        usuario.setId(99L);
        usuario.setEmail("test-id@umh.es");

        when(usuarioService.findAllUsuarios()).thenReturn(Collections.singletonList(usuario));

        mockMvc.perform(get("/registered"))
                .andExpect(content().string(containsString("99")));
    }
}