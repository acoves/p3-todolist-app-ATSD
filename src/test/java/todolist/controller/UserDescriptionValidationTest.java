package todolist.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
public class UserDescriptionValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testIdConCaracteresEspeciales() throws Exception {
        // When: ID con caracteres no numéricos
        mockMvc.perform(get("/registered/1a"))
                .andExpect(status().isBadRequest());
    }
}