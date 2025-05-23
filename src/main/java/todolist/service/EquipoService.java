package todolist.service;

import todolist.dto.EquipoData;
import todolist.dto.TareaData;
import todolist.dto.UsuarioData;
import todolist.model.Equipo;
import todolist.repository.EquipoRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EquipoService {

    @Autowired
    private EquipoRepository equipoRepository;
    @Autowired
    private ModelMapper modelMapper;


    // Se añade un equipo en la aplicación.
    // El nombre debe ser distinto de null
    // El nombre no debe estar registrado en la base de datos
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

    @Transactional(readOnly = true)
    public EquipoData findByNombre(String nombre) {
        Equipo equipo = equipoRepository.findByNombre(nombre).orElse(null);
        if (equipo == null) return null;
        else {
            return modelMapper.map(equipo, EquipoData.class);
        }
    }

    @Transactional(readOnly = true)
    public EquipoData findById(Long equipoId) {
        Equipo equipo = equipoRepository.findById(equipoId).orElse(null);
        if (equipo == null) return null;
        else {
            return modelMapper.map(equipo, EquipoData.class);
        }
    }

    @Transactional
    public EquipoData crearEquipo(String nombre) {
        Optional<Equipo> equipoBD = equipoRepository.findByNombre(nombre);
        if (equipoBD.isPresent())
            throw new EquipoServiceException("El equipo " + nombre + " ya está registrado");
        else if (nombre == null)
            throw new EquipoServiceException("El equipo no tiene nombre");
        else {
            Equipo equipoNuevo = modelMapper.map(new Equipo(), Equipo.class);
            equipoNuevo.setNombre(nombre);
            equipoNuevo = equipoRepository.save(equipoNuevo);

            return modelMapper.map(equipoNuevo, EquipoData.class);
        }
    }

    @Transactional
    public EquipoData recuperarEquipo(Long id) {
        Equipo equipo = equipoRepository.findById(id).orElse(null);

        return modelMapper.map(equipo, EquipoData.class);
    }

    @Transactional
    public List<EquipoData> findAllOrdenadoPorNombre() {
        // recuperamos todos los equipos
        List<Equipo> equipos;
        equipos = equipoRepository.findAll();

        // cambiamos el tipo de la lista de equipos
        List<EquipoData> equiposData = equipos.stream()
                .map(equipo -> modelMapper.map(equipo, EquipoData.class))
                .collect(Collectors.toList());
        
        // ordenamos la lista por nombre del equipo
        Collections.sort(equiposData, (a, b) -> a.getNombre().compareTo(b.getNombre()));
        return equiposData;
    }
}

