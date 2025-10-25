
package com.example.booking;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest
@AutoConfigureMockMvc
public class BookingControllerTest {
    @Autowired private MockMvc mockMvc;
    @Test
    public void registerAndAuthSmoke() throws Exception {
        mockMvc.perform(post("/api/user/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"test1\",\"password\":\"p\",\"role\":\"USER\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists());
        mockMvc.perform(post("/api/user/auth").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"test1\",\"password\":\"p\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists());
    }
}
