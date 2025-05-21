package todolist.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import todolist.dto.UsuarioData;
import todolist.service.UsuarioService;
import todolist.authentication.ManagerUserSession;
import todolist.service.UsuarioServiceException;
import java.util.List;

@Controller
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final ManagerUserSession managerUserSession;

    public UsuarioController(UsuarioService usuarioService, ManagerUserSession managerUserSession) {
        this.usuarioService = usuarioService;
        this.managerUserSession = managerUserSession;
    }

    // Nuevo endpoint para actualizar estado de usuario
    @PutMapping("/registered/{id}/status")
    @ResponseBody
    public ResponseEntity<?> updateUserStatus(
            @PathVariable Long id,
            @RequestParam boolean enabled
    ) {
        Long loggedUserId = managerUserSession.usuarioLogeado();

        // Verificar autenticación y auto-bloqueo
        if (loggedUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (id.equals(loggedUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            usuarioService.toggleUserStatus(id, enabled);
            return ResponseEntity.ok().build();
        } catch (UsuarioServiceException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/registered")
    public String listUsuarios(Model model) {
        Long usuarioId = managerUserSession.usuarioLogeado();

        // Verificar autenticación
        if (usuarioId == null) {
            return "redirect:/login";
        }

        try {
            // Obtener usuario logeado y lista de usuarios
            UsuarioData usuario = usuarioService.findById(usuarioId);
            List<UsuarioData> usuarios = usuarioService.findAllUsuarios();

            // Añadir atributos al modelo
            model.addAttribute("usuario", usuario);
            model.addAttribute("usuarios", usuarios);
            model.addAttribute("loggedIn", true);

            return "usersList";

        } catch (Exception e) {
            // Manejar errores mostrando un mensaje en la misma vista
            model.addAttribute("error", "Error al cargar los usuarios: " + e.getMessage());
            return "usersList"; // Mantenerse en la misma página
        }
    }

    @GetMapping("/registered/{id}")
    public String verUsuario(@PathVariable Long id, Model model) {
        Long usuarioLogeadoId = managerUserSession.usuarioLogeado();

        if (usuarioLogeadoId == null) {
            return "redirect:/login";
        }

        try {
            UsuarioData usuario = usuarioService.findById(id);
            model.addAttribute("loggedIn", true);
            model.addAttribute("usuarioLogeado", usuarioService.findById(usuarioLogeadoId));
            model.addAttribute("usuario", usuario);
            return "usersDescription";

        } catch (RuntimeException e) {
            return "redirect:/registered"; // Redirige si hay error
        }
    }
}