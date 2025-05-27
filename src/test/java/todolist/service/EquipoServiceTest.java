package todolist.service;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import todolist.dto.EquipoData;
import todolist.dto.UsuarioData;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class EquipoServiceTest {

    @Autowired
    EquipoService equipoService;

    @Autowired
    UsuarioService usuarioService;

    @Test
    public void crearRecuperarEquipo() {

        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");
        assertThat(equipo.getId()).isNotNull();

        EquipoData equipoBd = equipoService.recuperarEquipo(equipo.getId());
        assertThat(equipoBd).isNotNull();
        assertThat(equipoBd.getNombre()).isEqualTo("Proyecto 1");
    }

    @Test
    public void listadoEquiposOrdenAlfabetico() {

        equipoService.crearEquipo("Proyecto BBB");
        equipoService.crearEquipo("Proyecto AAA");


        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();


        assertThat(equipos).hasSize(2);
        assertThat(equipos.get(0).getNombre()).isEqualTo("Proyecto AAA");
        assertThat(equipos.get(1).getNombre()).isEqualTo("Proyecto BBB");
    }

    @Test
    public void añadirUsuarioAEquipoTest() {

        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@umh");
        usuario.setPassword("1234");
        usuario = usuarioService.registrar(usuario);
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1");


        equipoService.añadirUsuarioAEquipo(equipo.getId(), usuario.getId());


        List<UsuarioData> usuarios = equipoService.usuariosEquipo(equipo.getId());
        assertThat(usuarios).hasSize(1);
        assertThat(usuarios.get(0).getEmail()).isEqualTo("user@umh");
    }

    @Test
    public void recuperarEquiposDeUsuario() {

        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@umh");
        usuario.setPassword("1234");
        usuario = usuarioService.registrar(usuario);
        EquipoData equipo1 = equipoService.crearEquipo("Project 1");
        EquipoData equipo2 = equipoService.crearEquipo("Project 2");
        equipoService.añadirUsuarioAEquipo(equipo1.getId(), usuario.getId());
        equipoService.añadirUsuarioAEquipo(equipo2.getId(), usuario.getId());


        List<EquipoData> equipos = equipoService.equiposUsuario(usuario.getId());

        assertThat(equipos).hasSize(2);
        assertThat(equipos.get(0).getNombre()).isEqualTo("Project 1");
        assertThat(equipos.get(1).getNombre()).isEqualTo("Project 2");
    }
    @Test
    public void comprobarExcepciones() {
        assertThatThrownBy(() -> equipoService.recuperarEquipo(1L))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.añadirUsuarioAEquipo(1L, 1L))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.usuariosEquipo(1L))
                .isInstanceOf(EquipoServiceException.class);
        assertThatThrownBy(() -> equipoService.equiposUsuario(1L))
                .isInstanceOf(EquipoServiceException.class);


        EquipoData equipo = equipoService.crearEquipo("Project 1");
        assertThatThrownBy(() -> equipoService.añadirUsuarioAEquipo(equipo.getId(), 1L))
                .isInstanceOf(EquipoServiceException.class);
    }

    @Test
    public void eliminarUsuarioDeEquipoTest() {
        UsuarioData usuarioData = new UsuarioData();
        usuarioData.setEmail("user@umh");
        usuarioData.setPassword("1234");
        UsuarioData usuario = usuarioService.registrar(usuarioData);
        EquipoData equipo = equipoService.crearEquipo("Project X");
        equipoService.añadirUsuarioAEquipo(equipo.getId(), usuario.getId());

        equipoService.eliminarUsuarioDeEquipo(equipo.getId(), usuario.getId());

        List<UsuarioData> usuarios = equipoService.usuariosEquipo(equipo.getId());
        assertThat(usuarios).isEmpty();
    }

    @Test
    public void eliminarUsuarioInexistenteDeEquipoTest() {
        EquipoData equipo = equipoService.crearEquipo("Project Y");

        assertThatThrownBy(() -> equipoService.eliminarUsuarioDeEquipo(equipo.getId(), 999L))
                .isInstanceOf(EquipoServiceException.class);
    }

    @Test
    public void añadirUsuarioDuplicadoAEquipoTest() {
        UsuarioData usuarioData = new UsuarioData();
        usuarioData.setEmail("user2@umh");
        usuarioData.setPassword("5678");
        UsuarioData usuario = usuarioService.registrar(usuarioData);

        EquipoData equipo = equipoService.crearEquipo("Project Z");
        equipoService.añadirUsuarioAEquipo(equipo.getId(), usuario.getId());

        assertThatThrownBy(() -> equipoService.añadirUsuarioAEquipo(equipo.getId(), usuario.getId()))
                .isInstanceOf(EquipoServiceException.class);
    }

    @Test
    public void renombrarEquipoTest() {
        EquipoData equipo = equipoService.crearEquipo("Old Name");

        equipoService.renombrarEquipo(equipo.getId(), "New Name");

        EquipoData updatedEquipo = equipoService.recuperarEquipo(equipo.getId());
        assertThat(updatedEquipo.getNombre()).isEqualTo("New Name");
    }


}