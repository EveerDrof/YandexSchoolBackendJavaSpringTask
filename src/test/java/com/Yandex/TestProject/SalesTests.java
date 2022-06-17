package com.Yandex.TestProject;

import jakarta.transaction.Transactional;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
class SalesTests {
    private final TestingUtils testingUtils;
    private MockMvc mockMvc;

    @Autowired
    public SalesTests(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
        this.testingUtils = new TestingUtils(mockMvc);
    }

    @Test
    void salesItemsTest() throws Exception {
        JSONObject jsonObject = testingUtils.createImportPostingWithNested();
        testingUtils.postImport(jsonObject);
        mockMvc.perform(get("/sales").param("date", jsonObject.getString("updateDate")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].date").value("2022-02-01T12:00:00.000Z"))
                .andExpect(jsonPath("$.items[0].name").exists())
                .andExpect(jsonPath("$.items[0].type").exists())
                .andExpect(jsonPath("$.items[0].id").exists());
    }

    @Test
    void salesNotInRangeTest() throws Exception {
        JSONObject jsonObject = testingUtils.createImportPostingWithNested();
        testingUtils.postImport(jsonObject);
        mockMvc.perform(get("/sales").param("date", "2024-02-01T12:00:00.000Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void salesPriceUpdateTest() throws Exception {
        JSONObject jsonObject = testingUtils.createImportPostingWithNested();
        testingUtils.postImport(jsonObject);
        jsonObject.getJSONArray("items").getJSONObject(0).getJSONArray("children").getJSONObject(0)
                .put("price", 999999);
        jsonObject.put("updateDate", "2077-02-01T12:00:00.000Z");
        testingUtils.postImport(jsonObject);
        mockMvc.perform(get("/sales").param("date", "2077-02-01T12:00:00.000Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isNotEmpty())
                .andExpect(jsonPath("$.items[0].price").value(999999));
    }

    @Test
    void salesTimeBoundaries24hTest() throws Exception {
        JSONObject jsonObject = testingUtils.createImportPostingWithNested();
        testingUtils.postImport(jsonObject);
        mockMvc.perform(get("/sales").param("date", "2022-02-02T12:00:01.000Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
        mockMvc.perform(get("/sales").param("date", "2022-01-31T11:59:59.000Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void wrongTimeFormatShouldReturnError() throws Exception {
        JSONObject jsonObject = testingUtils.createImportPostingJSON();
        testingUtils.postImport(jsonObject);
        testingUtils.expectValidationFailed(
                mockMvc.perform(get("/sales").param("date", "2022-02-01T12:00:00.0000Z"))
        );
    }

}