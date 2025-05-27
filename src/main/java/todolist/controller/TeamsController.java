package todolist.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/teams/{teamId}/add-user")
    public String mostrarFormularioAñadirUsuario(
            @PathVariable("teamId") Long teamId,
            Model model
    ) {
        Long usuarioId = managerUserSession.usuarioLogeado();
        if (usuarioId == null) return "redirect:/login";

        try {
            UsuarioData usuarioLogeado = usuarioService.findById(usuarioId);
            EquipoData equipo = equipoService.recuperarEquipo(teamId);
            List<UsuarioData> usuariosDisponibles = equipoService.findAllUsuariosNoEnEquipo(teamId);

            model.addAttribute("equipo", equipo);
            model.addAttribute("usuarios", usuariosDisponibles);
            model.addAttribute("usuarioLogeado", usuarioLogeado);
            return "equipos/add-user";

        } catch (RuntimeException e) {
            model.addAttribute("error", "Error: " + e.getMessage());
            return "redirect:/teams";
        }
    }

    @PostMapping("/teams/{teamId}/add-user")
    public String añadirUsuarioAlEquipo(
            @PathVariable("teamId") Long teamId,
            @RequestParam("usuarioId") Long usuarioId
    ) {
        if (managerUserSession.usuarioLogeado() == null) return "redirect:/login";

        try {
            equipoService.añadirUsuarioAEquipo(teamId, usuarioId);
            return "redirect:/teams/" + teamId + "/members";

        } catch (RuntimeException e) {
            return "redirect:/teams/" + teamId + "/members?error=" + e.getMessage();
        }
    }

    @PostMapping("/teams/{teamId}/remove-user/{usuarioId}")
    public String eliminarUsuarioDelEquipo(
            @PathVariable("teamId") Long teamId,
            @PathVariable("usuarioId") Long usuarioId
    ) {
        if (managerUserSession.usuarioLogeado() == null) return "redirect:/login";

        try {
            equipoService.eliminarUsuarioDeEquipo(teamId, usuarioId);
            return "redirect:/teams/" + teamId + "/members";

        } catch (RuntimeException e) {
            return "redirect:/teams/" + teamId + "/members?error=" + e.getMessage();
        }
    }
}