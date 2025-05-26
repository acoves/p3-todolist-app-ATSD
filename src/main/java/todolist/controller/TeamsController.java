package todolist.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import todolist.authentication.ManagerUserSession;
import todolist.dto.EquipoData;
import todolist.dto.UsuarioData;
import todolist.service.EquipoService;
import todolist.service.UsuarioService;
import java.util.Collections;
import java.util.List;

@Controller
public class TeamsController {

    private final EquipoService equipoService;
    private final ManagerUserSession managerUserSession;
    private final UsuarioService usuarioService;

    public TeamsController(EquipoService equipoService,
                           ManagerUserSession managerUserSession,
                           UsuarioService usuarioService) {
        this.equipoService = equipoService;
        this.managerUserSession = managerUserSession;
        this.usuarioService = usuarioService;
    }

    // Método listTeams ORIGINAL (sin cambios)
    @GetMapping("/teams")
    public String listTeams(Model model) {
        Long usuarioId = managerUserSession.usuarioLogeado();

        if (usuarioId == null) {
            return "redirect:/login";
        }

        try {
            UsuarioData usuarioLogeado = usuarioService.findById(usuarioId);
            List<EquipoData> teams = equipoService.findAllOrdenadoPorNombre();

            model.addAttribute("teams", teams != null ? teams : Collections.emptyList());
            model.addAttribute("loggedIn", true);
            model.addAttribute("usuarioLogeado", usuarioLogeado);
            model.addAttribute("usuario", usuarioLogeado);

            return "teamsList";

        } catch (RuntimeException e) {
            model.addAttribute("error", "Error al cargar equipos: " + e.getMessage());
            model.addAttribute("loggedIn", true);
            model.addAttribute("usuarioLogeado", usuarioService.findById(usuarioId));
            model.addAttribute("usuario", usuarioService.findById(usuarioId));
            return "teamsList";
        }
    }

    // Método listTeamMembers ORIGINAL (solo añadidos mínimos para navbar)
    @GetMapping("/teams/{teamId}/members")
    public String listTeamMembers(@PathVariable Long teamId, Model model) {
        Long usuarioId = managerUserSession.usuarioLogeado();

        if (usuarioId == null) {
            return "redirect:/login";
        }

        try {
            UsuarioData usuario = usuarioService.findById(usuarioId);
            EquipoData equipo = equipoService.recuperarEquipo(teamId);
            List<UsuarioData> miembros = equipoService.usuariosEquipo(teamId);

            model.addAttribute("equipo", equipo);
            model.addAttribute("miembros", miembros != null ? miembros : Collections.emptyList());
            model.addAttribute("loggedIn", true);
            model.addAttribute("usuarioLogeado", usuario);
            model.addAttribute("usuario", usuario);

            return "teamDetails";

        } catch (RuntimeException e) {
            model.addAttribute("error", "Error al cargar miembros: " + e.getMessage());
            model.addAttribute("loggedIn", true);
            model.addAttribute("usuarioLogeado", usuarioService.findById(usuarioId));
            model.addAttribute("usuario", usuarioService.findById(usuarioId));
            return "teamDetails";
        }
    }
}