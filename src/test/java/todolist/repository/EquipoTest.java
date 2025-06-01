package todolist.repository;

import todolist.model.Equipo;
import todolist.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class EquipoTest {

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;
    // Test para comprobar que se crea un equipo con un nombre
    @Test
    public void crearEquipo() {
        Equipo equipo = new Equipo("Project P1");
        assertThat(equipo.getNombre()).isEqualTo("Project P1");
    }
    // Test para comprobar que se crea un equipo y se guarda en la base de datos
    @Test
    @Transactional
    public void grabarYBuscarEquipo() {

        Equipo equipo = new Equipo("Project P1");

        Equipo equipo1 = new Equipo();

        equipo = new Equipo("Project P1");


        equipoRepository.save(equipo);

        Long equipoId = equipo.getId();
        assertThat(equipoId).isNotNull();
        Equipo equipoDB = equipoRepository.findById(equipoId).orElse(null);
        assertThat(equipoDB).isNotNull();
        assertThat(equipoDB.getNombre()).isEqualTo("Project P1");
    }
    // Test para comprobar que se crea un equipo y se guarda en la base de datos
    @Test
    public void comprobarIgualdadEquipos() {

        Equipo equipo1 = new Equipo("Project P1");
        Equipo equipo2 = new Equipo("Project P2");
        Equipo equipo3 = new Equipo("Project P2");


        assertThat(equipo1).isNotEqualTo(equipo2);
        assertThat(equipo2).isEqualTo(equipo3);
        assertThat(equipo2.hashCode()).isEqualTo(equipo3.hashCode());


        equipo1.setId(1L);
        equipo2.setId(1L);
        equipo3.setId(2L);

        assertThat(equipo1).isEqualTo(equipo2);
        assertThat(equipo2).isNotEqualTo(equipo3);
    }
    // Test para comprobar que se añade un usuario a un equipo
    @Test
    @Transactional
    public void comprobarRelacionBaseDatos() {

        Equipo equipo = new Equipo("Project 1");
        equipoRepository.save(equipo);

        Usuario usuario = new Usuario("user@umh");
        usuarioRepository.save(usuario);



        equipo.addUsuario(usuario);


        Equipo equipoBD = equipoRepository.findById(equipo.getId()).orElse(null);
        Usuario usuarioBD = usuarioRepository.findById(usuario.getId()).orElse(null);

        assertThat(equipo.getUsuarios()).hasSize(1);
        assertThat(equipo.getUsuarios()).contains(usuario);
        assertThat(usuario.getEquipos()).hasSize(1);
        assertThat(usuario.getEquipos()).contains(equipo);
    }
    // Test para comprobar que se recuperan todos los equipos de la base de datos
    @Test
    @Transactional
    public void comprobarFindAll() {

        equipoRepository.save(new Equipo("Proyecto 2"));
        equipoRepository.save(new Equipo("Proyecto 3"));

        List<Equipo> equipos = equipoRepository.findAll();

        assertThat(equipos).hasSize(2);
    }

}