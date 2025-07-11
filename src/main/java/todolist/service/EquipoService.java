package todolist.service;

import todolist.dto.EquipoData;
import todolist.dto.UsuarioData;
import todolist.model.Equipo;
import todolist.model.Usuario;
import todolist.repository.EquipoRepository;
import todolist.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EquipoService {

    @Autowired
    private EquipoRepository equipoRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private UsuarioRepository usuarioRepository;

    // Método para registrar un nuevo equipo
    @Transactional
    public EquipoData registrar(EquipoData equipo) {
        Optional<Equipo> equipoBD = equipoRepository.findByNombre(equipo.getNombre());
        if (equipoBD.isPresent())
            throw new EquipoServiceException("El equipo " + equipo.getNombre() + " ya está registrado");
        else if (equipo.getNombre() == null)
            throw new EquipoServiceException("El equipo no tiene nombre");
        else {
            Equipo equipoNuevo = modelMapper.map(equipo, Equipo.class);
            equipoNuevo = equipoRepository.save(equipoNuevo);
            return modelMapper.map(equipoNuevo, EquipoData.class);
        }
    }
    // Método para encontrar un equipo por su ID
    @Transactional(readOnly = true)
    public EquipoData findById(Long equipoId) {
        Equipo equipo = equipoRepository.findById(equipoId).orElse(null);
        if (equipo == null) return null;
        else {
            return modelMapper.map(equipo, EquipoData.class);
        }
    }
    // Método para recuperar un equipo por su ID
    @Transactional
    public EquipoData recuperarEquipo(Long id) {
        Equipo equipo = equipoRepository.findById(id).orElse(null);
        if (equipo == null)
            throw new EquipoServiceException("El equipo no existe");
        return modelMapper.map(equipo, EquipoData.class);
    }
    // Método para encontrar todos los equipos ordenados por nombre
    @Transactional
    public List<EquipoData> findAllOrdenadoPorNombre() {
        List<Equipo> equipos;
        equipos = equipoRepository.findAll();

        List<EquipoData> equiposData = equipos.stream()
                .map(equipo -> modelMapper.map(equipo, EquipoData.class))
                .collect(Collectors.toList());

        Collections.sort(equiposData, (a, b) -> a.getNombre().compareTo(b.getNombre()));
        return equiposData;
    }
    // Método para añadir un usuario a un equipo
    @Transactional
    public void añadirUsuarioAEquipo(Long equipoId, Long usuarioId) {
        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new EquipoServiceException("El equipo no existe"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EquipoServiceException("El usuario no existe"));

        if (equipo.getUsuarios().contains(usuario)) {
            throw new EquipoServiceException("El usuario ya pertenece al equipo");
        }

        equipo.addUsuario(usuario);

        equipoRepository.save(equipo);
        usuarioRepository.save(usuario);
    }
    // Método para obtener los usuarios de un equipo
    @Transactional
    public List<UsuarioData> usuariosEquipo(Long idEquipo) {
        Equipo equipo = equipoRepository.findById(idEquipo)
                .orElseThrow(() -> new EquipoServiceException("El equipo no existe"));

        return equipo.getUsuarios().stream()
                .sorted(Comparator.comparing(Usuario::getNombre))
                .map(usuario -> modelMapper.map(usuario, UsuarioData.class))
                .collect(Collectors.toList());
    }
    // Método para obtener los equipos de un usuario
    @Transactional
    public List<EquipoData> equiposUsuario(long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario == null)
            throw new EquipoServiceException("El usuario no existe");

        List<EquipoData> equipos = usuario.getEquipos().stream()
                .map(equipo -> modelMapper.map(equipo, EquipoData.class))
                .collect(Collectors.toList());
        return equipos;

    }
    // Método para encontrar todos los usuarios que no están en un equipo específico
    @Transactional(readOnly = true)
    public List<UsuarioData> findAllUsuariosNoEnEquipo(Long equipoId) {
        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new EquipoServiceException("Equipo no encontrado"));

        Iterable<Usuario> usuariosIterable = usuarioRepository.findAll();
        List<Usuario> todosUsuarios = new ArrayList<>();
        usuariosIterable.forEach(todosUsuarios::add);

        List<Usuario> usuariosNoEnEquipo = todosUsuarios.stream()
                .filter(usuario -> !equipo.getUsuarios().contains(usuario))
                .collect(Collectors.toList());

        return usuariosNoEnEquipo.stream()
                .map(usuario -> modelMapper.map(usuario, UsuarioData.class))
                .collect(Collectors.toList());
    }
    // Método para eliminar un usuario de un equipo
    @Transactional
    public void eliminarUsuarioDeEquipo(Long equipoId, Long usuarioId) {
        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new EquipoServiceException("Equipo no encontrado"));
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EquipoServiceException("Usuario no encontrado"));

        equipo.getUsuarios().remove(usuario);
        usuario.getEquipos().remove(equipo);
    }
    // Método para renombrar un equipo
    @Transactional
    public void renombrarEquipo(Long equipoId, String nuevoNombre) {
        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new EquipoServiceException("Equipo no encontrado"));
        equipo.setNombre(nuevoNombre);
        equipoRepository.save(equipo);
    }
    // Método para eliminar un equipo
    @Transactional
    public void eliminarEquipo(Long equipoId) {
        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new EquipoServiceException("Equipo no encontrado"));
        equipo.getUsuarios().forEach(usuario -> usuario.getEquipos().remove(equipo));
        equipoRepository.delete(equipo);
    }
    // Método para crear un equipo con nombre
    @Transactional
    public EquipoData crearEquipo(String nombre) {
        Optional<Equipo> equipoBD = equipoRepository.findByNombre(nombre);

        if (equipoBD.isPresent()) {
            throw new EquipoServiceException("El equipo " + nombre + " ya está registrado");
        } else if (nombre == null) {
            throw new EquipoServiceException("El equipo no tiene nombre");
        } else {
            Equipo equipoNuevo = new Equipo();
            equipoNuevo.setNombre(nombre);
            equipoNuevo = equipoRepository.save(equipoNuevo);
            return modelMapper.map(equipoNuevo, EquipoData.class);
        }
    }
}