package com.smart.home.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.home.domain.AirConditioner;
import com.smart.home.dto.AirConditionerRequest;
import com.smart.home.repository.AirConditionerRepository;
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
class AirConditionerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AirConditionerRepository airConditionerRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void tearDown() {
        airConditionerRepository.deleteAll();
        roomRepository.deleteAll();
    }

    @Test
    void createAirConditionerReturnsCreatedAirConditioner() throws Exception {
        var roomResponse = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.smart.home.dto.RoomRequest("Living Room"))))
                .andReturn();

        String roomContent = roomResponse.getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(roomContent).get("id").asLong();

        AirConditionerRequest request = new AirConditionerRequest("AC Unit", roomId);

        mockMvc.perform(post("/api/air-conditioners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("AC Unit"));
    }

    @Test
    void getAllAirConditionersReturnsAllAirConditioners() throws Exception {
        var roomResponse = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.smart.home.dto.RoomRequest("Living Room"))))
                .andReturn();

        String roomContent = roomResponse.getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(roomContent).get("id").asLong();

        mockMvc.perform(post("/api/air-conditioners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AirConditionerRequest("AC1", roomId))));

        mockMvc.perform(post("/api/air-conditioners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AirConditionerRequest("AC2", roomId))));

        mockMvc.perform(get("/api/air-conditioners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAirConditionersByRoomReturnsAirConditionersForRoom() throws Exception {
        var roomResponse = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.smart.home.dto.RoomRequest("Bedroom"))))
                .andReturn();

        String roomContent = roomResponse.getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(roomContent).get("id").asLong();

        mockMvc.perform(post("/api/air-conditioners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AirConditionerRequest("Bedroom AC", roomId))));

        mockMvc.perform(get("/api/air-conditioners/room/" + roomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Bedroom AC"));
    }

    @Test
    void turnOnTurnsAirConditionerOn() throws Exception {
        var roomResponse = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.smart.home.dto.RoomRequest("Living Room"))))
                .andReturn();

        String roomContent = roomResponse.getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(roomContent).get("id").asLong();

        var acResponse = mockMvc.perform(post("/api/air-conditioners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AirConditionerRequest("AC", roomId))))
                .andReturn();

        String acContent = acResponse.getResponse().getContentAsString();
        Long acId = objectMapper.readTree(acContent).get("id").asLong();

        mockMvc.perform(put("/api/air-conditioners/" + acId + "/on"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.powerState").value("ON"))
                .andExpect(jsonPath("$.thermostatMode").value("COOL"));
    }

    @Test
    void turnOffTurnsAirConditionerOff() throws Exception {
        var roomResponse = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.smart.home.dto.RoomRequest("Living Room"))))
                .andReturn();

        String roomContent = roomResponse.getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(roomContent).get("id").asLong();

        var acResponse = mockMvc.perform(post("/api/air-conditioners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AirConditionerRequest("AC", roomId))))
                .andReturn();

        String acContent = acResponse.getResponse().getContentAsString();
        Long acId = objectMapper.readTree(acContent).get("id").asLong();

        mockMvc.perform(put("/api/air-conditioners/" + acId + "/on"));
        mockMvc.perform(put("/api/air-conditioners/" + acId + "/off"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.powerState").value("OFF"))
                .andExpect(jsonPath("$.thermostatMode").value("OFF"));
    }

    @Test
    void turnOnByRoomTurnsAirConditionerOn() throws Exception {
        var roomResponse = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.smart.home.dto.RoomRequest("Kitchen"))))
                .andReturn();

        String roomContent = roomResponse.getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(roomContent).get("id").asLong();

        var acResponse = mockMvc.perform(post("/api/air-conditioners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AirConditionerRequest("Kitchen AC", roomId))))
                .andReturn();

        String acContent = acResponse.getResponse().getContentAsString();
        Long acId = objectMapper.readTree(acContent).get("id").asLong();

        mockMvc.perform(put("/api/air-conditioners/rooms/" + roomId + "/" + acId + "/on"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.powerState").value("ON"));
    }

    @Test
    void turnOffByRoomTurnsAirConditionerOff() throws Exception {
        var roomResponse = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.smart.home.dto.RoomRequest("Kitchen"))))
                .andReturn();

        String roomContent = roomResponse.getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(roomContent).get("id").asLong();

        var acResponse = mockMvc.perform(post("/api/air-conditioners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AirConditionerRequest("Kitchen AC", roomId))))
                .andReturn();

        String acContent = acResponse.getResponse().getContentAsString();
        Long acId = objectMapper.readTree(acContent).get("id").asLong();

        mockMvc.perform(put("/api/air-conditioners/rooms/" + roomId + "/" + acId + "/on"));
        mockMvc.perform(put("/api/air-conditioners/rooms/" + roomId + "/" + acId + "/off"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.powerState").value("OFF"));
    }

    @Test
    void turnOnAllByRoomTurnsAllAirConditionersOn() throws Exception {
        var roomResponse = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.smart.home.dto.RoomRequest("Living Room"))))
                .andReturn();

        String roomContent = roomResponse.getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(roomContent).get("id").asLong();

        mockMvc.perform(post("/api/air-conditioners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AirConditionerRequest("AC1", roomId))));
        mockMvc.perform(post("/api/air-conditioners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AirConditionerRequest("AC2", roomId))));

        mockMvc.perform(put("/api/air-conditioners/rooms/" + roomId + "/all/on"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void turnOffAllByRoomTurnsAllAirConditionersOff() throws Exception {
        var roomResponse = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.smart.home.dto.RoomRequest("Living Room"))))
                .andReturn();

        String roomContent = roomResponse.getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(roomContent).get("id").asLong();

        mockMvc.perform(post("/api/air-conditioners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AirConditionerRequest("AC1", roomId))));
        mockMvc.perform(post("/api/air-conditioners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AirConditionerRequest("AC2", roomId))));

        mockMvc.perform(put("/api/air-conditioners/rooms/" + roomId + "/all/on"));
        mockMvc.perform(put("/api/air-conditioners/rooms/" + roomId + "/all/off"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void deleteAirConditionerRemovesAirConditioner() throws Exception {
        var roomResponse = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.smart.home.dto.RoomRequest("Living Room"))))
                .andReturn();

        String roomContent = roomResponse.getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(roomContent).get("id").asLong();

        var acResponse = mockMvc.perform(post("/api/air-conditioners")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AirConditionerRequest("AC", roomId))))
                .andReturn();

        String acContent = acResponse.getResponse().getContentAsString();
        Long acId = objectMapper.readTree(acContent).get("id").asLong();

        mockMvc.perform(delete("/api/air-conditioners/" + acId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/air-conditioners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
