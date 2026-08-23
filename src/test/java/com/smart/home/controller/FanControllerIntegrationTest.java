package com.smart.home.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.home.dto.FanRequest;
import com.smart.home.dto.FanSpeedRequest;
import com.smart.home.repository.FanRepository;
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
class FanControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FanRepository fanRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void tearDown() {
        fanRepository.deleteAll();
        roomRepository.deleteAll();
    }

    @Test
    void createFanReturnsCreatedFan() throws Exception {
        var roomResponse = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.smart.home.dto.RoomRequest("Living Room"))))
                .andReturn();

        String roomContent = roomResponse.getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(roomContent).get("id").asLong();

        FanRequest request = new FanRequest("Ceiling Fan", roomId);

        mockMvc.perform(post("/api/fans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Ceiling Fan"));
    }

    @Test
    void getAllFansReturnsAllFans() throws Exception {
        var roomResponse = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.smart.home.dto.RoomRequest("Living Room"))))
                .andReturn();

        String roomContent = roomResponse.getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(roomContent).get("id").asLong();

        mockMvc.perform(post("/api/fans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new FanRequest("Fan1", roomId))));

        mockMvc.perform(post("/api/fans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new FanRequest("Fan2", roomId))));

        mockMvc.perform(get("/api/fans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getFansByRoomReturnsFansForRoom() throws Exception {
        var roomResponse = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.smart.home.dto.RoomRequest("Bedroom"))))
                .andReturn();

        String roomContent = roomResponse.getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(roomContent).get("id").asLong();

        mockMvc.perform(post("/api/fans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new FanRequest("Bedroom Fan", roomId))));

        mockMvc.perform(get("/api/fans/room/" + roomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Bedroom Fan"));
    }

    @Test
    void turnOnTurnsFanOn() throws Exception {
        var roomResponse = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.smart.home.dto.RoomRequest("Living Room"))))
                .andReturn();

        String roomContent = roomResponse.getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(roomContent).get("id").asLong();

        var fanResponse = mockMvc.perform(post("/api/fans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new FanRequest("Fan", roomId))))
                .andReturn();

        String fanContent = fanResponse.getResponse().getContentAsString();
        Long fanId = objectMapper.readTree(fanContent).get("id").asLong();

        mockMvc.perform(put("/api/fans/" + fanId + "/on"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.powerState").value("ON"))
                .andExpect(jsonPath("$.speed").value(1));
    }

    @Test
    void turnOffTurnsFanOff() throws Exception {
        var roomResponse = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.smart.home.dto.RoomRequest("Living Room"))))
                .andReturn();

        String roomContent = roomResponse.getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(roomContent).get("id").asLong();

        var fanResponse = mockMvc.perform(post("/api/fans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new FanRequest("Fan", roomId))))
                .andReturn();

        String fanContent = fanResponse.getResponse().getContentAsString();
        Long fanId = objectMapper.readTree(fanContent).get("id").asLong();

        mockMvc.perform(put("/api/fans/" + fanId + "/on"));
        mockMvc.perform(put("/api/fans/" + fanId + "/off"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.powerState").value("OFF"))
                .andExpect(jsonPath("$.speed").value(0));
    }

    @Test
    void updateFanSpeedUpdatesSpeed() throws Exception {
        var roomResponse = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.smart.home.dto.RoomRequest("Living Room"))))
                .andReturn();

        String roomContent = roomResponse.getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(roomContent).get("id").asLong();

        var fanResponse = mockMvc.perform(post("/api/fans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new FanRequest("Fan", roomId))))
                .andReturn();

        String fanContent = fanResponse.getResponse().getContentAsString();
        Long fanId = objectMapper.readTree(fanContent).get("id").asLong();

        FanSpeedRequest speedRequest = new FanSpeedRequest(2);

        mockMvc.perform(put("/api/fans/" + fanId + "/speed")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(speedRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.speed").value(2))
                .andExpect(jsonPath("$.powerState").value("ON"));
    }

    @Test
    void updateFanSpeedToZeroTurnsOff() throws Exception {
        var roomResponse = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.smart.home.dto.RoomRequest("Living Room"))))
                .andReturn();

        String roomContent = roomResponse.getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(roomContent).get("id").asLong();

        var fanResponse = mockMvc.perform(post("/api/fans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new FanRequest("Fan", roomId))))
                .andReturn();

        String fanContent = fanResponse.getResponse().getContentAsString();
        Long fanId = objectMapper.readTree(fanContent).get("id").asLong();

        mockMvc.perform(put("/api/fans/" + fanId + "/on"));

        FanSpeedRequest speedRequest = new FanSpeedRequest(0);

        mockMvc.perform(put("/api/fans/" + fanId + "/speed")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(speedRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.speed").value(0))
                .andExpect(jsonPath("$.powerState").value("OFF"));
    }

    @Test
    void updateFanSpeedByRoomUpdatesSpeed() throws Exception {
        var roomResponse = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.smart.home.dto.RoomRequest("Kitchen"))))
                .andReturn();

        String roomContent = roomResponse.getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(roomContent).get("id").asLong();

        var fanResponse = mockMvc.perform(post("/api/fans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new FanRequest("Kitchen Fan", roomId))))
                .andReturn();

        String fanContent = fanResponse.getResponse().getContentAsString();
        Long fanId = objectMapper.readTree(fanContent).get("id").asLong();

        FanSpeedRequest speedRequest = new FanSpeedRequest(2);

        mockMvc.perform(put("/api/fans/rooms/" + roomId + "/" + fanId + "/speed")
                        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(speedRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.speed").value(2))
                .andExpect(jsonPath("$.powerState").value("ON"));
    }

    @Test
    void turnOnByRoomTurnsFanOn() throws Exception {
        var roomResponse = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.smart.home.dto.RoomRequest("Kitchen"))))
                .andReturn();

        String roomContent = roomResponse.getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(roomContent).get("id").asLong();

        var fanResponse = mockMvc.perform(post("/api/fans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new FanRequest("Kitchen Fan", roomId))))
                .andReturn();

        String fanContent = fanResponse.getResponse().getContentAsString();
        Long fanId = objectMapper.readTree(fanContent).get("id").asLong();

        mockMvc.perform(put("/api/fans/rooms/" + roomId + "/" + fanId + "/on"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.powerState").value("ON"));
    }

    @Test
    void turnOffByRoomTurnsFanOff() throws Exception {
        var roomResponse = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.smart.home.dto.RoomRequest("Kitchen"))))
                .andReturn();

        String roomContent = roomResponse.getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(roomContent).get("id").asLong();

        var fanResponse = mockMvc.perform(post("/api/fans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new FanRequest("Kitchen Fan", roomId))))
                .andReturn();

        String fanContent = fanResponse.getResponse().getContentAsString();
        Long fanId = objectMapper.readTree(fanContent).get("id").asLong();

        mockMvc.perform(put("/api/fans/rooms/" + roomId + "/" + fanId + "/on"));
        mockMvc.perform(put("/api/fans/rooms/" + roomId + "/" + fanId + "/off"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.powerState").value("OFF"));
    }

    @Test
    void turnOnAllByRoomTurnsAllFansOn() throws Exception {
        var roomResponse = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.smart.home.dto.RoomRequest("Living Room"))))
                .andReturn();

        String roomContent = roomResponse.getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(roomContent).get("id").asLong();

        mockMvc.perform(post("/api/fans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new FanRequest("Fan1", roomId))));
        mockMvc.perform(post("/api/fans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new FanRequest("Fan2", roomId))));

        mockMvc.perform(put("/api/fans/rooms/" + roomId + "/all/on"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void turnOffAllByRoomTurnsAllFansOff() throws Exception {
        var roomResponse = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.smart.home.dto.RoomRequest("Living Room"))))
                .andReturn();

        String roomContent = roomResponse.getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(roomContent).get("id").asLong();

        mockMvc.perform(post("/api/fans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new FanRequest("Fan1", roomId))));
        mockMvc.perform(post("/api/fans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new FanRequest("Fan2", roomId))));

        mockMvc.perform(put("/api/fans/rooms/" + roomId + "/all/on"));
        mockMvc.perform(put("/api/fans/rooms/" + roomId + "/all/off"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void deleteFanRemovesFan() throws Exception {
        var roomResponse = mockMvc.perform(post("/api/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new com.smart.home.dto.RoomRequest("Living Room"))))
                .andReturn();

        String roomContent = roomResponse.getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(roomContent).get("id").asLong();

        var fanResponse = mockMvc.perform(post("/api/fans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new FanRequest("Fan", roomId))))
                .andReturn();

        String fanContent = fanResponse.getResponse().getContentAsString();
        Long fanId = objectMapper.readTree(fanContent).get("id").asLong();

        mockMvc.perform(delete("/api/fans/" + fanId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/fans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
