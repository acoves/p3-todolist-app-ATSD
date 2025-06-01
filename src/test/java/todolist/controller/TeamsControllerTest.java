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
import todolist.service.EquipoServiceException;
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
    // Tests para el controlador TeamsController
    @Test
    void listTeams_UsuarioNoLogeado_RedirigeALogin() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        mockMvc.perform(get("/teams"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(managerUserSession).usuarioLogeado();
    }
    // Test para listar equipos cuando el usuario está logeado
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
    // Test para listar equipos cuando no hay equipos disponibles
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
    // Test para listar miembros de un equipo
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
    // Test para listar miembros de un equipo válido
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
    // Test para añadir un usuario a un equipo
    @Test
    void añadirUsuarioAlEquipo_Exitoso_RedirigeConExito() throws Exception {
        Long userId = 1L;
        Long teamId = 1L;

        when(managerUserSession.usuarioLogeado()).thenReturn(userId);
        doNothing().when(equipoService).añadirUsuarioAEquipo(eq(teamId), eq(userId));

        mockMvc.perform(post("/teams/{teamId}/add-user", teamId))
                .andExpect(redirectedUrl("/teams/" + teamId + "/members"));
    }
    // Test para eliminar un usuario de un equipo
    @Test
    void eliminarUsuarioDelEquipo_Exitoso_RedirigeConExito() throws Exception {
        Long userId = 1L;
        Long teamId = 1L;

        when(managerUserSession.usuarioLogeado()).thenReturn(userId);
        doNothing().when(equipoService).eliminarUsuarioDeEquipo(teamId, userId);

        mockMvc.perform(post("/teams/{teamId}/remove-user", teamId))
                .andExpect(redirectedUrl("/teams/" + teamId + "/members"));
    }
    // Test para eliminar un usuario de un equipo cuando el usuario no es miembro
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
    // Test para mostrar el formulario de creación de un equipo
    @Test
    void mostrarFormularioCreacion_UsuarioNoLogeado_RedirigeALogin() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        mockMvc.perform(get("/teams/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
    // Test para mostrar el formulario de creación de un equipo cuando el usuario está logeado
    @Test
    void mostrarFormularioCreacion_UsuarioLogeado_MuestraFormulario() throws Exception {
        Long userId = 1L;
        UsuarioData usuario = new UsuarioData();
        usuario.setId(userId);

        when(managerUserSession.usuarioLogeado()).thenReturn(userId);
        when(usuarioService.findById(userId)).thenReturn(usuario);

        mockMvc.perform(get("/teams/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("create-team"))
                .andExpect(model().attributeExists("teamData"));
    }
    // Test para crear un equipo exitosamente
    @Test
    void crearEquipo_Duplicado_MuestraError() throws Exception {
        Long userId = 1L;
        UsuarioData usuario = new UsuarioData();
        usuario.setId(userId);

        when(managerUserSession.usuarioLogeado()).thenReturn(userId);
        when(usuarioService.findById(userId)).thenReturn(usuario);
        doThrow(new EquipoServiceException("Equipo duplicado")).when(equipoService).crearEquipo("Equipo Duplicado");

        mockMvc.perform(post("/teams")
                        .param("nombre", "Equipo Duplicado"))
                .andExpect(status().isOk())
                .andExpect(view().name("create-team"))
                .andExpect(model().attributeExists("error"));
    }
    // Test para crear un equipo exitosamente
    @Test
    void mostrarFormularioEdicion_UsuarioNoAdmin_Redirige() throws Exception {
        Long userId = 1L;
        when(managerUserSession.usuarioLogeado()).thenReturn(userId);
        when(usuarioService.isAdmin(userId)).thenReturn(false);

        mockMvc.perform(get("/teams/1/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/teams?error=Acceso no autorizado para edición"));
    }
    // Test para mostrar el formulario de edición de un equipo
    @Test
    void mostrarFormularioEdicion_AdminYEquipoValido_MuestraFormulario() throws Exception {
        Long userId = 1L;
        Long teamId = 1L;
        UsuarioData usuario = new UsuarioData();
        usuario.setId(userId);
        EquipoData equipo = new EquipoData();
        equipo.setId(teamId);
        equipo.setNombre("Equipo Existente");

        when(managerUserSession.usuarioLogeado()).thenReturn(userId);
        when(usuarioService.isAdmin(userId)).thenReturn(true);
        when(usuarioService.findById(userId)).thenReturn(usuario);
        when(equipoService.recuperarEquipo(teamId)).thenReturn(equipo);

        mockMvc.perform(get("/teams/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("editTeam"))
                .andExpect(model().attribute("equipo", equipo));
    }
    // Test para mostrar el formulario de edición de un equipo cuando no existe
    @Test
    void mostrarFormularioEdicion_EquipoNoExiste_RedirigeConError() throws Exception {
        Long userId = 1L;
        Long teamId = 999L;

        when(managerUserSession.usuarioLogeado()).thenReturn(userId);
        when(usuarioService.isAdmin(userId)).thenReturn(true);
        when(equipoService.recuperarEquipo(teamId)).thenThrow(new RuntimeException("Equipo no encontrado"));

        mockMvc.perform(get("/teams/999/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/teams?error=Error al cargar el equipo: Equipo no encontrado"));
    }
    // Test para editar un equipo exitosamente
    @Test
    void editarEquipo_Valido_RedirigeATeams() throws Exception {
        Long userId = 1L;
        Long teamId = 1L;

        when(managerUserSession.usuarioLogeado()).thenReturn(userId);
        when(usuarioService.isAdmin(userId)).thenReturn(true);
        doNothing().when(equipoService).renombrarEquipo(teamId, "Nuevo Nombre");

        mockMvc.perform(post("/teams/1/edit")
                        .param("nombre", "Nuevo Nombre"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/teams"));
    }
    // Test para editar un equipo cuando el usuario no es administrador
    @Test
    void editarEquipo_UsuarioNoAdmin_Redirige() throws Exception {
        Long userId = 1L;
        Long teamId = 1L;

        when(managerUserSession.usuarioLogeado()).thenReturn(userId);
        when(usuarioService.isAdmin(userId)).thenReturn(false);

        mockMvc.perform(post("/teams/1/edit")
                        .param("nombre", "Nuevo Nombre"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/teams?error=Acceso no autorizado"));
    }
    // Test para editar un equipo cuando ocurre un error en el servicio
    @Test
    void editarEquipo_ErrorServicio_RedirigeConError() throws Exception {
        Long userId = 1L;
        Long teamId = 1L;

        when(managerUserSession.usuarioLogeado()).thenReturn(userId);
        when(usuarioService.isAdmin(userId)).thenReturn(true);
        doThrow(new RuntimeException("Error al renombrar")).when(equipoService).renombrarEquipo(teamId, "Nombre Invalido");

        mockMvc.perform(post("/teams/1/edit")
                        .param("nombre", "Nombre Invalido"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/teams/1/edit"))
                .andExpect(flash().attributeExists("error"));
    }
    // Test para eliminar un equipo
    @Test
    void eliminarEquipo_AdminExitoso_RedirigeATeams() throws Exception {
        Long userId = 1L;
        Long teamId = 1L;

        when(managerUserSession.usuarioLogeado()).thenReturn(userId);
        when(usuarioService.isAdmin(userId)).thenReturn(true);
        doNothing().when(equipoService).eliminarEquipo(teamId);

        mockMvc.perform(post("/teams/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/teams"));
    }
    // Test para eliminar un equipo cuando el usuario no es administrador
    @Test
    void eliminarEquipo_NoAdmin_RedirigeConError() throws Exception {
        Long userId = 1L;
        Long teamId = 1L;

        when(managerUserSession.usuarioLogeado()).thenReturn(userId);
        when(usuarioService.isAdmin(userId)).thenReturn(false);

        mockMvc.perform(post("/teams/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/teams?error=Acceso no autorizado"));
    }
    // Test para eliminar un equipo cuando ocurre un error en el servicio
    @Test
    void eliminarEquipo_ErrorServicio_RedirigeConError() throws Exception {
        Long userId = 1L;
        Long teamId = 1L;

        when(managerUserSession.usuarioLogeado()).thenReturn(userId);
        when(usuarioService.isAdmin(userId)).thenReturn(true);
        doThrow(new RuntimeException("Error al eliminar")).when(equipoService).eliminarEquipo(teamId);

        mockMvc.perform(post("/teams/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/teams?error=Error al eliminar"));
    }
}