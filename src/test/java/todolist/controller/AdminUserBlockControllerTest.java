package todolist.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import todolist.service.UsuarioService;
import todolist.authentication.ManagerUserSession;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
class AdminUserBlockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private ManagerUserSession managerUserSession;

    @Test
    void updateUserStatus_whenAdminBlocksUser_shouldReturnOk() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(2L); // Admin ID

        mockMvc.perform(put("/registered/1/status?enabled=false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(usuarioService).toggleUserStatus(1L, false);
    }

    @Test
    void updateUserStatus_whenAdminBlocksSelf_shouldReturnForbidden() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L); // Mismo ID que el path

        mockMvc.perform(put("/registered/1/status?enabled=false"))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).toggleUserStatus(any(), anyBoolean());
    }

    @Test
    void updateUserStatus_whenUnauthenticated_shouldReturnUnauthorized() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        mockMvc.perform(put("/registered/1/status?enabled=false"))
                .andExpect(status().isUnauthorized());
    }
}