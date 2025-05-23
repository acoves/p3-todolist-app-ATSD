package todolist.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "usuarios")
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private String email;
    private String nombre;
    private String password;
    @Column(name = "fecha_nacimiento")
    @Temporal(TemporalType.DATE)
    private Date fechaNacimiento;

    // Campo para administrador (valor por defecto: false)
    @Column(name = "admin")
    private boolean admin = false;

    // Campo para habilitado (valor por defecto: true)
    @Column(name = "enabled")
    private boolean enabled = true;

    // Relación OneToMany con Tarea, lazy por defecto
    @OneToMany(mappedBy = "usuario")
    Set<Tarea> tareas = new HashSet<>();

    // Relación ManyToMany con Equipo
    @ManyToMany(mappedBy = "usuarios")
    Set<Equipo> equipos = new HashSet<>();

    // Constructor vacío necesario para JPA/Hibernate
    public Usuario() {}

    // Constructor público con el email
    public Usuario(String email) {
        this.email = email;
    }

    // Getters y setters para todos los campos

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Date getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(Date fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    // Getters para las relaciones

    public Set<Tarea> getTareas() {
        return tareas;
    }

    public Set<Equipo> getEquipos() {
        return equipos;
    }

    // Método helper para añadir una tarea
    public void addTarea(Tarea tarea) {
        if (tareas.contains(tarea)) return;
        tareas.add(tarea);
        if (tarea.getUsuario() != this) {
            tarea.setUsuario(this);
        }
    }

    // Métodos equals y hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        if (id != null && usuario.id != null)
            return Objects.equals(id, usuario.id);
        return email.equals(usuario.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}