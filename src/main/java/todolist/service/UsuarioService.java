package todolist.service;

import todolist.dto.UsuarioData;
import todolist.model.Usuario;
import todolist.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.Optional;

@Service
public class UsuarioService {

    Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    public enum LoginStatus {LOGIN_OK, USER_NOT_FOUND, ERROR_PASSWORD}

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ModelMapper modelMapper;

    // Método nuevo para verificar existencia de administrador
    @Transactional(readOnly = true)
    public boolean existsByAdmin(boolean admin) {
        return usuarioRepository.existsByAdmin(admin);
    }

    // Nuevo método para cambiar el estado enabled
    @Transactional
    public void toggleUserStatus(Long userId, boolean enabled) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new UsuarioServiceException("Usuario no encontrado"));
        usuario.setEnabled(enabled);
        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public LoginStatus login(String eMail, String password) {
        Optional<Usuario> usuario = usuarioRepository.findByEmail(eMail);
        if (!usuario.isPresent()) {
            return LoginStatus.USER_NOT_FOUND;
        } else if (!usuario.get().getPassword().equals(password)) {
            return LoginStatus.ERROR_PASSWORD;
        } else {
            return LoginStatus.LOGIN_OK;
        }
    }

    // Registro modificado para validar administrador único
    @Transactional
    public UsuarioData registrar(UsuarioData usuario) {
        Optional<Usuario> usuarioBD = usuarioRepository.findByEmail(usuario.getEmail());
        if (usuarioBD.isPresent()) {
            throw new UsuarioServiceException("El usuario " + usuario.getEmail() + " ya está registrado");
        } else if (usuario.getEmail() == null) {
            throw new UsuarioServiceException("El usuario no tiene email");
        } else if (usuario.getPassword() == null) {
            throw new UsuarioServiceException("El usuario no tiene password");
        } else {
            // Validar si el nuevo usuario es admin y ya existe uno
            Usuario usuarioNuevo = modelMapper.map(usuario, Usuario.class);
            usuarioNuevo.setEnabled(true);
            if (usuarioNuevo.isAdmin() && usuarioRepository.existsByAdmin(true)) {
                throw new UsuarioServiceException("Ya existe un administrador registrado");
            }

            usuarioNuevo = usuarioRepository.save(usuarioNuevo);
            return modelMapper.map(usuarioNuevo, UsuarioData.class);
        }
    }

    @Transactional(readOnly = true)
    public UsuarioData findByEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        return (usuario != null) ? modelMapper.map(usuario, UsuarioData.class) : null;
    }

    @Transactional(readOnly = true)
    public UsuarioData findById(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioServiceException("Usuario no encontrado"));
        return modelMapper.map(usuario, UsuarioData.class);
    }

    @Transactional(readOnly = true)
    public List<UsuarioData> findAllUsuarios() {
        Iterable<Usuario> usuarios = usuarioRepository.findAll();
        return StreamSupport.stream(usuarios.spliterator(), false)
                .map(usuario -> modelMapper.map(usuario, UsuarioData.class))
                .collect(Collectors.toList());
    }
}