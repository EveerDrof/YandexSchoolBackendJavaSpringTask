package com.Yandex.TestProject;

import jakarta.transaction.Transactional;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
class TestProjectApplicationTests {
    private MockMvc mockMvc;
    private String firstImportObjectId = "069cb8d7-bbdd-47d3-ad8f-82ef4c269df1";

    public TestProjectApplicationTests(@Autowired MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    private ResultActions postImport() throws Exception {
        JSONObject jsonObject = new JSONObject();
        JSONArray items = new JSONArray();
        JSONObject item = new JSONObject();
        item.put("type", "CATEGORY");
        item.put("name", "Товар");
        item.put("id", firstImportObjectId);
        item.put("parentId", "null");
        items.put(item);
        jsonObject.put("items", items);
        jsonObject.put("updateDate", "2022-02-01T12:00:00.000Z");
        return mockMvc.perform(post("/imports").contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toString()));
    }

    @Test
    void contextLoads() {

    }

    @Test
    void emptyImportsPostBodyShouldReturn400() throws Exception {
        mockMvc.perform(
                post("/imports")
        ).andExpect(status().isBadRequest());
    }

    @Test
    void postCorrectImportShouldReturn200() throws Exception {
        postImport().andExpect(status().isOk());
        ;
    }

    @Test
    void getNonExistentNode() throws Exception {
        mockMvc.perform(
                get("/nodes/3fa85f64-5717-4562-b3fc-2c963f66a3331312312312132")
        ).andExpect(jsonPath("$.code").value(404)).andExpect(jsonPath("$.message")
                .value("Item not found"));
    }

    @Test
    void postAndGetExistingNode() throws Exception {
        postImport();
        mockMvc.perform(get("/nodes/" + firstImportObjectId)).andExpect(status().isOk());
    }

}
