package todolist.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import todolist.authentication.ManagerUserSession;
import todolist.dto.EquipoData;
import todolist.dto.UsuarioData;
import todolist.service.EquipoService;
import todolist.service.UsuarioService;

import javax.servlet.http.HttpSession;
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
        if (usuarioId == null) return "redirect:/login";

        try {
            UsuarioData usuarioLogeado = usuarioService.findById(usuarioId);
            List<EquipoData> teams = equipoService.findAllOrdenadoPorNombre();

            model.addAttribute("teams", teams != null ? teams : Collections.emptyList());
            model.addAttribute("loggedIn", true);
            model.addAttribute("usuarioLogeado", usuarioLogeado);
            model.addAttribute("usuario", usuarioLogeado);

            return "teamsList";

        } catch (RuntimeException e) {
            UsuarioData usuarioLogeado = usuarioService.findById(usuarioId);
            model.addAttribute("error", "Error al cargar equipos: " + e.getMessage());
            model.addAttribute("loggedIn", true);
            model.addAttribute("usuarioLogeado", usuarioLogeado);
            model.addAttribute("usuario", usuarioLogeado);
            return "teamsList";
        }
    }

    @GetMapping("/teams/{teamId}/members")
    public String listTeamMembers(@PathVariable Long teamId, Model model) {
        Long usuarioId = managerUserSession.usuarioLogeado();
        if (usuarioId == null) return "redirect:/login";

        try {
            UsuarioData usuarioLogeado = usuarioService.findById(usuarioId);
            EquipoData equipo = equipoService.recuperarEquipo(teamId);
            List<UsuarioData> miembros = equipoService.usuariosEquipo(teamId);

            model.addAttribute("equipo", equipo);
            model.addAttribute("miembros", miembros != null ? miembros : Collections.emptyList());
            model.addAttribute("loggedIn", true);
            model.addAttribute("usuarioLogeado", usuarioLogeado);
            model.addAttribute("usuario", usuarioLogeado);

            return "teamDetails";

        } catch (RuntimeException e) {
            UsuarioData usuarioLogeado = usuarioService.findById(usuarioId);
            model.addAttribute("equipo", null);
            model.addAttribute("error", "Error al cargar miembros: " + e.getMessage());
            model.addAttribute("loggedIn", true);
            model.addAttribute("usuarioLogeado", usuarioLogeado);
            model.addAttribute("usuario", usuarioLogeado);
            return "teamDetails";
        }
    }

    @PostMapping("/teams/{teamId}/add-user")
    public String añadirUsuarioAlEquipo(
            @PathVariable("teamId") Long teamId,
            RedirectAttributes redirectAttributes) {

        Long usuarioLogeadoId = managerUserSession.usuarioLogeado();
        if (usuarioLogeadoId == null) return "redirect:/login";

        try {
            equipoService.añadirUsuarioAEquipo(teamId, usuarioLogeadoId);
            return "redirect:/teams/" + teamId + "/members";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/teams/" + teamId + "/members";
        }
    }

    @PostMapping("/teams/{teamId}/remove-user")
    public String eliminarUsuarioDelEquipo(
            @PathVariable("teamId") Long teamId,
            RedirectAttributes redirectAttributes) {

        Long usuarioLogeadoId = managerUserSession.usuarioLogeado();
        if (usuarioLogeadoId == null) return "redirect:/login";

        try {
            equipoService.eliminarUsuarioDeEquipo(teamId, usuarioLogeadoId);
            return "redirect:/teams/" + teamId + "/members";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/teams/" + teamId + "/members";
        }
    }

    @GetMapping("/teams/{teamId}/edit")
    public String mostrarFormularioEdicion(@PathVariable Long teamId,
                                           Model model,
                                           HttpSession session) {

        Long usuarioId = managerUserSession.usuarioLogeado();
        if (usuarioId == null) {
            return "redirect:/login";
        }

        if (!usuarioService.isAdmin(usuarioId)) {
            return "redirect:/teams?error=Acceso no autorizado para edición";
        }

        try {
            EquipoData equipo = equipoService.recuperarEquipo(teamId);
            UsuarioData usuarioLogeado = usuarioService.findById(usuarioId);

            model.addAttribute("equipo", equipo);
            model.addAttribute("usuario", usuarioLogeado);
            model.addAttribute("loggedIn", true);

            return "editTeam";

        } catch (RuntimeException e) {
            return "redirect:/teams?error=Error al cargar el equipo: " + e.getMessage();
        }
    }

    @PostMapping("/teams/{teamId}/edit")
    public String editarEquipo(
            @PathVariable("teamId") Long teamId,
            @RequestParam("nombre") String nuevoNombre,
            RedirectAttributes redirectAttributes
    ) {
        Long usuarioId = managerUserSession.usuarioLogeado();

        if (usuarioId == null || !usuarioService.isAdmin(usuarioId)) {
            return "redirect:/teams?error=Acceso no autorizado";
        }

        try {
            equipoService.renombrarEquipo(teamId, nuevoNombre);
            return "redirect:/teams";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el equipo");
            return "redirect:/teams/" + teamId + "/edit";
        }
    }

    @PostMapping("/teams/{teamId}/delete")
    public String eliminarEquipo(@PathVariable Long teamId) {
        Long usuarioId = managerUserSession.usuarioLogeado();
        if (usuarioId == null || !usuarioService.isAdmin(usuarioId)) {
            return "redirect:/teams?error=Acceso no autorizado";
        }

        try {
            equipoService.eliminarEquipo(teamId);
            return "redirect:/teams";
        } catch (RuntimeException e) {
            return "redirect:/teams?error=" + e.getMessage();
        }
    }
}