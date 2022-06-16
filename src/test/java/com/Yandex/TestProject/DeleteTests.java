package com.Yandex.TestProject;

import jakarta.transaction.Transactional;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
class DeleteTests {
    private final TestingUtils testingUtils;
    private MockMvc mockMvc;

    @Autowired
    public DeleteTests(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
        this.testingUtils = new TestingUtils(mockMvc);
    }

    @Test
    void deleteOfferTest() throws Exception {
        JSONObject jsonObject = testingUtils.createImportPostingWithNested();
        testingUtils.postImport(jsonObject);
        mockMvc.perform(delete("/delete/" + testingUtils.getNestedImportObjectId()))
                .andExpect(status().isOk());
        mockMvc.perform(get("/nodes/" + testingUtils.getNestedImportObjectId()))
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void deleteCategoryTest() throws Exception {
        JSONObject jsonObject = testingUtils.createImportPostingWithNested();
        testingUtils.postImport(jsonObject);
        mockMvc.perform(delete("/delete/" + testingUtils.getFirstImportObjectId()))
                .andExpect(status().isOk());
        mockMvc.perform(get("/nodes/" + testingUtils.getFirstImportObjectId()))
                .andExpect(jsonPath("$.code").value(404));
        mockMvc.perform(get("/nodes/" + testingUtils.getNestedImportObjectId()))
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void deleteNonExistingNode() throws Exception {
        mockMvc.perform(delete("/delete/" + testingUtils.getFirstImportObjectId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Item not found"));
    }
}