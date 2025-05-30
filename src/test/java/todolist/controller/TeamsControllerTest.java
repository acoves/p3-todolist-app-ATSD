package todolist.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import todolist.authentication.ManagerUserSession;
import todolist.dto.EquipoData;
import todolist.dto.UsuarioData;
import todolist.service.EquipoService;
import todolist.service.UsuarioService;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TeamsControllerTest {

    @Mock
    private EquipoService equipoService;

    @Mock
    private ManagerUserSession managerUserSession;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private TeamsController teamsController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(teamsController).build();
    }

    @Test
    void listTeams_UsuarioNoLogeado_RedirigeALogin() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        mockMvc.perform(get("/teams"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(managerUserSession).usuarioLogeado();
    }

    @Test
    void listTeams_UsuarioLogeado_MuestraEquipos() throws Exception {
        Long userId = 1L;
        UsuarioData usuario = new UsuarioData();
        usuario.setId(userId);

        EquipoData equipo1 = new EquipoData();
        equipo1.setId(1L);
        equipo1.setNombre("Equipo A");

        EquipoData equipo2 = new EquipoData();
        equipo2.setId(2L);
        equipo2.setNombre("Equipo B");

        List<EquipoData> equipos = Arrays.asList(equipo1, equipo2);

        when(managerUserSession.usuarioLogeado()).thenReturn(userId);
        when(usuarioService.findById(userId)).thenReturn(usuario);
        when(equipoService.findAllOrdenadoPorNombre()).thenReturn(equipos);

        mockMvc.perform(get("/teams"))
                .andExpect(status().isOk())
                .andExpect(view().name("teamsList"))
                .andExpect(model().attribute("teams", equipos));
    }

    @Test
    void listTeams_ErrorServicio_MuestraError() throws Exception {
        Long userId = 1L;
        UsuarioData usuario = new UsuarioData();
        usuario.setId(userId);

        when(managerUserSession.usuarioLogeado()).thenReturn(userId);
        when(usuarioService.findById(userId)).thenReturn(usuario);
        when(equipoService.findAllOrdenadoPorNombre()).thenThrow(new RuntimeException("Error simulado"));

        mockMvc.perform(get("/teams"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void listTeamMembers_EquipoInexistente_MuestraError() throws Exception {
        Long userId = 1L;
        UsuarioData usuario = new UsuarioData();
        usuario.setId(userId);

        when(managerUserSession.usuarioLogeado()).thenReturn(userId);
        when(usuarioService.findById(userId)).thenReturn(usuario);
        when(equipoService.recuperarEquipo(anyLong())).thenThrow(new RuntimeException("Equipo no existe"));

        mockMvc.perform(get("/teams/999/members"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void listTeamMembers_EquipoValido_MuestraMiembros() throws Exception {
        Long userId = 1L;
        Long teamId = 1L;

        UsuarioData usuario = new UsuarioData();
        usuario.setId(userId);

        EquipoData equipo = new EquipoData();
        equipo.setId(teamId);
        equipo.setNombre("Equipo X");

        UsuarioData miembro = new UsuarioData();
        miembro.setId(2L);
        miembro.setNombre("Usuario Miembro");

        List<UsuarioData> miembros = Collections.singletonList(miembro);

        when(managerUserSession.usuarioLogeado()).thenReturn(userId);
        when(usuarioService.findById(userId)).thenReturn(usuario);
        when(equipoService.recuperarEquipo(teamId)).thenReturn(equipo);
        when(equipoService.usuariosEquipo(teamId)).thenReturn(miembros);

        mockMvc.perform(get("/teams/" + teamId + "/members"))
                .andExpect(model().attribute("equipo", equipo))
                .andExpect(model().attribute("miembros", miembros));
    }

    @Test
    void añadirUsuarioAlEquipo_Exitoso_RedirigeConExito() throws Exception {
        Long userId = 1L;
        Long teamId = 1L;

        when(managerUserSession.usuarioLogeado()).thenReturn(userId);
        doNothing().when(equipoService).añadirUsuarioAEquipo(eq(teamId), eq(userId));

        mockMvc.perform(post("/teams/{teamId}/add-user", teamId))
                .andExpect(redirectedUrl("/teams/" + teamId + "/members"));
    }

    @Test
    void eliminarUsuarioDelEquipo_Exitoso_RedirigeConExito() throws Exception {
        Long userId = 1L;
        Long teamId = 1L;

        when(managerUserSession.usuarioLogeado()).thenReturn(userId);
        doNothing().when(equipoService).eliminarUsuarioDeEquipo(teamId, userId);

        mockMvc.perform(post("/teams/{teamId}/remove-user", teamId))
                .andExpect(redirectedUrl("/teams/" + teamId + "/members"));
    }

    @Test
    void accionesProtegidas_RedirigenSinAutenticacion() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        mockMvc.perform(get("/teams"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        mockMvc.perform(get("/teams/1/members"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        mockMvc.perform(post("/teams/1/add-user"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        mockMvc.perform(post("/teams/1/remove-user"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}