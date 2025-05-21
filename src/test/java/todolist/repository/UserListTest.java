package todolist.repository;

import todolist.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class UserListTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    public void testFindAllReturnsAllUsers() {
        // Crear 2 usuarios
        Usuario usuario1 = new Usuario("repo-user1@umh.es");
        usuarioRepository.save(usuario1);

        Usuario usuario2 = new Usuario("repo-user2@umh.es");
        usuarioRepository.save(usuario2);

        Iterable<Usuario> usuarios = usuarioRepository.findAll();
        assertThat(usuarios).hasSize(2);
    }
}