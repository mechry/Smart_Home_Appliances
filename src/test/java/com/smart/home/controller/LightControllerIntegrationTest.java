package com.smart.home.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.home.domain.Light;
import com.smart.home.domain.PowerState;
import com.smart.home.dto.LightRequest;
import com.smart.home.repository.LightRepository;
import com.smart.home.repository.RoomRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class LightControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LightRepository lightRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void tearDown() {
        lightRepository.deleteAll();
        roomRepository.deleteAll();
    }

    @Test
    void createLightReturnsCreatedLight() throws Exception {
        var roomResponse = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.smart.home.dto.RoomRequest("Living Room"))))
                .andReturn();

        String roomContent = roomResponse.getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(roomContent).get("id").asLong();

        LightRequest request = new LightRequest("Lamp", roomId);

        mockMvc.perform(post("/api/lights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Lamp"));
    }

    @Test
    void getAllLightsReturnsAllLights() throws Exception {
        var roomResponse = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.smart.home.dto.RoomRequest("Living Room"))))
                .andReturn();

        String roomContent = roomResponse.getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(roomContent).get("id").asLong();

        mockMvc.perform(post("/api/lights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LightRequest("Lamp1", roomId))));

        mockMvc.perform(post("/api/lights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LightRequest("Lamp2", roomId))));

        mockMvc.perform(get("/api/lights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getLightsByRoomReturnsLightsForRoom() throws Exception {
        var roomResponse = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.smart.home.dto.RoomRequest("Bedroom"))))
                .andReturn();

        String roomContent = roomResponse.getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(roomContent).get("id").asLong();

        mockMvc.perform(post("/api/lights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LightRequest("Bedroom Lamp", roomId))));

        mockMvc.perform(get("/api/lights/room/" + roomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Bedroom Lamp"));
    }

    @Test
    void turnOnTurnsLightOn() throws Exception {
        var roomResponse = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.smart.home.dto.RoomRequest("Living Room"))))
                .andReturn();

        String roomContent = roomResponse.getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(roomContent).get("id").asLong();

        var lightResponse = mockMvc.perform(post("/api/lights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LightRequest("Lamp", roomId))))
                .andReturn();

        String lightContent = lightResponse.getResponse().getContentAsString();
        Long lightId = objectMapper.readTree(lightContent).get("id").asLong();

        mockMvc.perform(put("/api/lights/" + lightId + "/on"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.powerState").value("ON"))
                .andExpect(jsonPath("$.switchPosition").value("ON"));
    }

    @Test
    void turnOffTurnsLightOff() throws Exception {
        var roomResponse = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.smart.home.dto.RoomRequest("Living Room"))))
                .andReturn();

        String roomContent = roomResponse.getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(roomContent).get("id").asLong();

        var lightResponse = mockMvc.perform(post("/api/lights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LightRequest("Lamp", roomId))))
                .andReturn();

        String lightContent = lightResponse.getResponse().getContentAsString();
        Long lightId = objectMapper.readTree(lightContent).get("id").asLong();

        mockMvc.perform(put("/api/lights/" + lightId + "/on"));
        mockMvc.perform(put("/api/lights/" + lightId + "/off"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.powerState").value("OFF"))
                .andExpect(jsonPath("$.switchPosition").value("OFF"));
    }

    @Test
    void turnOnByRoomTurnsLightOn() throws Exception {
        var roomResponse = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.smart.home.dto.RoomRequest("Kitchen"))))
                .andReturn();

        String roomContent = roomResponse.getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(roomContent).get("id").asLong();

        var lightResponse = mockMvc.perform(post("/api/lights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LightRequest("Kitchen Light", roomId))))
                .andReturn();

        String lightContent = lightResponse.getResponse().getContentAsString();
        Long lightId = objectMapper.readTree(lightContent).get("id").asLong();

        mockMvc.perform(put("/api/lights/rooms/" + roomId + "/" + lightId + "/on"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.powerState").value("ON"));
    }

    @Test
    void turnOffByRoomTurnsLightOff() throws Exception {
        var roomResponse = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.smart.home.dto.RoomRequest("Kitchen"))))
                .andReturn();

        String roomContent = roomResponse.getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(roomContent).get("id").asLong();

        var lightResponse = mockMvc.perform(post("/api/lights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LightRequest("Kitchen Light", roomId))))
                .andReturn();

        String lightContent = lightResponse.getResponse().getContentAsString();
        Long lightId = objectMapper.readTree(lightContent).get("id").asLong();

        mockMvc.perform(put("/api/lights/rooms/" + roomId + "/" + lightId + "/on"));
        mockMvc.perform(put("/api/lights/rooms/" + roomId + "/" + lightId + "/off"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.powerState").value("OFF"));
    }

    @Test
    void turnOnAllByRoomTurnsAllLightsOn() throws Exception {
        var roomResponse = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.smart.home.dto.RoomRequest("Living Room"))))
                .andReturn();

        String roomContent = roomResponse.getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(roomContent).get("id").asLong();

        mockMvc.perform(post("/api/lights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LightRequest("Light1", roomId))));
        mockMvc.perform(post("/api/lights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LightRequest("Light2", roomId))));

        mockMvc.perform(put("/api/lights/rooms/" + roomId + "/all/on"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void turnOffAllByRoomTurnsAllLightsOff() throws Exception {
        var roomResponse = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.smart.home.dto.RoomRequest("Living Room"))))
                .andReturn();

        String roomContent = roomResponse.getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(roomContent).get("id").asLong();

        mockMvc.perform(post("/api/lights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LightRequest("Light1", roomId))));
        mockMvc.perform(post("/api/lights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LightRequest("Light2", roomId))));

        mockMvc.perform(put("/api/lights/rooms/" + roomId + "/all/on"));
        mockMvc.perform(put("/api/lights/rooms/" + roomId + "/all/off"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void deleteLightRemovesLight() throws Exception {
        var roomResponse = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.smart.home.dto.RoomRequest("Living Room"))))
                .andReturn();

        String roomContent = roomResponse.getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(roomContent).get("id").asLong();

        var lightResponse = mockMvc.perform(post("/api/lights")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LightRequest("Lamp", roomId))))
                .andReturn();

        String lightContent = lightResponse.getResponse().getContentAsString();
        Long lightId = objectMapper.readTree(lightContent).get("id").asLong();

        mockMvc.perform(delete("/api/lights/" + lightId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/lights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
