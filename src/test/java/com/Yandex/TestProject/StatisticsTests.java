package com.Yandex.TestProject;

import jakarta.transaction.Transactional;
import org.hamcrest.core.IsNull;
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
class StatisticsTests {
    private final TestingUtils testingUtils;
    private MockMvc mockMvc;

    @Autowired
    public StatisticsTests(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
        this.testingUtils = new TestingUtils(mockMvc);
    }

    @Test
    void statisticsTest() throws Exception {
        JSONObject jsonObject = testingUtils.createImportPostingWithNested();
        testingUtils.postImport(jsonObject);
        JSONObject category = jsonObject.getJSONArray("items").getJSONObject(0);
        JSONObject offer = category.getJSONArray("children").getJSONObject(0);
        offer.put("price", 999999);
        jsonObject.put("updateDate", "2033-02-01T12:00:00.000Z");
        testingUtils.postImport(jsonObject);
        offer.put("price", 11111);
        jsonObject.put("updateDate", "2044-02-01T12:00:00.000Z");
        testingUtils.postImport(jsonObject);
        mockMvc.perform(get("/node/" + category.getString("id") + "/statistic")
                        .param("dateStart", "2000-02-01T12:00:00.000Z")
                        .param("dateEnd", "2090-02-01T12:00:00.000Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isNotEmpty())
                .andExpect(jsonPath("$.items[0].id").value(category.getString("id")));
    }

    @Test
    void wrongDateTimeFormatShouldReturnError() throws Exception {
        JSONObject jsonObject = testingUtils.createImportPostingJSON();
        testingUtils.postImport(jsonObject);
        testingUtils.expectValidationFailed(mockMvc.perform(get(
                "/node/" + testingUtils.getFirstImportObjectId() + "/statistic")
                .param("dateStart", "2000-0201T12:00:00.000Z")
                .param("dateEnd", "209002-01T12:00:00.000Z"))
        );
    }

    private void testPriceWithinDate(int price, int index) throws Exception {
        mockMvc.perform(get("/node/" + testingUtils.getFirstImportObjectId() + "/statistic")
                        .param("dateStart", "2022-02-01T00:00:00.000Z")
                        .param("dateEnd", "2022-02-01T14:00:00.000Z"))
                .andExpect(jsonPath("$.items[" + index + "].price").value(price));
    }

    @Test
    void multipleUpdatesTest() throws Exception {
        JSONObject jsonObject = testingUtils.createImportPostingWithNested();
        JSONObject offer = jsonObject.getJSONArray("items").getJSONObject(0);
        offer.put("price", 1000);
        offer.put("type", "OFFER");
        testingUtils.postImport(jsonObject);
        testPriceWithinDate(1000, 0);
        offer.put("price", 2000);
        testingUtils.postImport(jsonObject);
        testPriceWithinDate(2000, 1);
        offer.put("price", 3000);
        testingUtils.postImport(jsonObject);
        testPriceWithinDate(3000, 2);
    }

    private void getStatisticAndExpectNotFoundError(String id) throws Exception {
        mockMvc.perform(get("/node/" + testingUtils.getFirstImportObjectId() + "/statistic")
                        .param("dateStart", "2022-02-01T00:00:00.000Z")
                        .param("dateEnd", "2022-02-01T14:00:00.000Z"))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Item not found"));
    }

    @Test
    void getNonExistentNodeShouldReturnError() throws Exception {
        getStatisticAndExpectNotFoundError(testingUtils.getFirstImportObjectId());
    }

    @Test
    void getDeletedNodeStatisticsShouldReturnError() throws Exception {
        JSONObject jsonObject = testingUtils.createImportPostingWithNested();
        testingUtils.postImport(jsonObject);
        mockMvc.perform(delete("/delete/" + testingUtils.getFirstImportObjectId()));
        mockMvc.perform(get("/node/" + testingUtils.getFirstImportObjectId() + "/statistic")
                        .param("dateStart", "2022-02-01T00:00:00.000Z")
                        .param("dateEnd", "2022-02-01T14:00:00.000Z"))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Item not found"));
    }

    @Test
    void getDeletedNestedNodeStatisticsShouldReturnError() throws Exception {
        JSONObject jsonObject = testingUtils.createImportPostingWithNested();
        testingUtils.postImport(jsonObject);
        mockMvc.perform(delete("/delete/" + testingUtils.getNestedImportObjectId()));
        mockMvc.perform(get("/node/" + testingUtils.getFirstImportObjectId() + "/statistic")
                        .param("dateStart", "2022-02-01T00:00:00.000Z")
                        .param("dateEnd", "2022-02-01T14:00:00.000Z"))
                .andExpect(jsonPath("$.items[0].price").value(IsNull.nullValue()));
    }
}