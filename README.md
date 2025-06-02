# 📝 ToDo List App – Team Management Module (v1.2.0)

Welcome to the **Team Management Module** of the ToDo List App. This feature allows users to join or leave teams, create new teams, and enables admins to rename or delete existing teams. Built with **Spring Boot**, **JPA**, **Thymeleaf**, and tested using **TDD**, this module enhances collaborative task management.

---

## 🚀 Features

### Team Membership

* Join or leave teams via UI buttons.
* Prevent duplicate team membership.
* Bidirectional sync between users and teams in the database.

### Team Administration (Admin-only)

* Rename teams.
* Delete teams and related relationships.
* Access protected via admin check.

### Team Creation

* Create new teams from a simple form.
* Avoid duplicates and blank names.

---

## 🧱 Database Schema

Built with **PostgreSQL**. Main tables involved:

* `usuario`: Registered users.
* `equipo`: Represents teams.
* `tarea`: User tasks.
* `equipo_usuario`: Many-to-many link table.


---

## 🧱 Endpoints

### Team Membership

| Method | Endpoint                     | Description  |
| ------ | ---------------------------- | ------------ |
| POST   | `/teams/{teamId}/addUser`    | Join a team  |
| POST   | `/teams/{teamId}/removeUser` | Leave a team |

### Team Admin

| Method | Endpoint                 | Description                      |
| ------ | ------------------------ | -------------------------------- |
| GET    | `/teams/{teamId}/edit`   | Show rename form (admin only)    |
| POST   | `/teams/{teamId}/edit`   | Process team rename (admin only) |
| POST   | `/teams/{teamId}/delete` | Delete team (admin only)         |

### Team Creation

| Method | Endpoint        | Description       |
| ------ | --------------- | ----------------- |
| POST   | `/teams/create` | Create a new team |

---

## 🧠 Service Logic Highlights

**EquipoService.java**

* Validates input and existence of teams/users.
* Throws custom exceptions with clear messages.
* Keeps JPA relationships consistent.

```java
@Transactional
public void añadirUsuarioAEquipo(Long idEquipo, Long idUsuario) {
    // Validates and prevents duplicates
}

@Transactional
public void eliminarUsuarioDeEquipo(Long equipoId, Long usuarioId) {
    // Updates both ends of the relationship
}

@Transactional
public EquipoData crearEquipo(String nombre) {
    // Validates name and prevents duplicates
}

@Transactional
public EquipoData renombrarEquipo(Long equipoId, String nuevoNombre) {
    // Admin-only rename with validation
}

@Transactional
public void eliminarEquipo(Long equipoId) {
    // Deletes a team and related mappings
}
```

---

## 🧪 Testing

### Unit Tests – `EquipoServiceTest`

* `testCrearEquipo()`: Valid creation and validation failures.
* `testAñadirUsuarioAEquipo()`: Ensures proper team-user link.
* `añadirUsuarioDuplicadoAEquipoTest()`: Prevents double membership.
* `testEliminarUsuarioDeEquipo()`: Correct relationship removal.
* `eliminarUsuarioInexistenteDeEquipoTest()`: Handles edge case.
* `testRenombrarEquipo()`: Ensures rename updates DB correctly.
* `testEliminarEquipo()`: Deletes and checks for consistency.

### Integration Tests – `TeamsControllerTest`

* `testCreateTeam()`: Form submission and DB insertion.
* `testAddUserToTeam()`: Adds member via POST request.
* `testRemoveUserFromTeam()`: Removes via endpoint.
* `testRenameTeam()` / `testDeleteTeam()`: Admin operations.
* Edge cases like `TeamNotFound` tested for robust error handling.

---

## 🎨 Templates

* `teamDetails.html`: Dynamic buttons for join/leave.
* `editTeam.html`: Rename form (admin only).
* `teamList.html`: Admin options visible only to admins.
* `create-team.html`: Simple form for team creation.

---

## 🔐 Security

Authorization enforced in controllers:

```java
if (usuarioId == null || !usuarioService.isAdmin(usuarioId)) {
  return "redirect:/teams?error=Acceso no autorizado";
}
```

Ensures admin-only access to sensitive endpoints.

---

## 🔧 Running the Module

### Prerequisites

* Java 8+
* PostgreSQL or compatible database

### Commands

```bash
$ ./mvn spring-boot:run
```

Then visit:
[http://localhost:8080/teams](http://localhost:8080/teams)

---

## 🧾 Documentation

Detailed explanation available in `/doc/exercise3.md`:

* Endpoints list
* Service logic
* Test classes
* Thymeleaf views
* Exception handling

---
## Docker Commands

To run the application with Docker:

```bash
docker pull acoves/p3-todolistapp
docker run -p 8080:8080 acoves/p3-todolistapp
```
--- 

## 🌐 Useful Links

* GitHub: [github.com/acoves/p3-todolist-app-ATSD](https://github.com/acoves/p3-todolist-app-ATSD)
* Trello: [Project Board](https://trello.com/invite/b/67e275084f990f292deb22ad/ATTId1d9bb29fd24e1f08359a3d9bf56dde546F9F226/p2-p3-to-do-list-app)
* Docker: [https://hub.docker.com/r/acoves/p3-todolistapp](https://hub.docker.com/r/acoves/p3-todolistapp)
---

**Version:** 1.2.0
**Author:** Alejandro Coves Bolaños
**Course:** Técnicas Ágiles de Desarrollo de Software (ATSD)
