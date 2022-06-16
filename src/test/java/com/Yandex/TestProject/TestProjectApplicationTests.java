package com.Yandex.TestProject;

import jakarta.transaction.Transactional;
import org.hamcrest.core.IsNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
class TestProjectApplicationTests {
    private final TestingUtils testingUtils;
    private MockMvc mockMvc;

    @Autowired
    public TestProjectApplicationTests(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
        this.testingUtils = new TestingUtils(mockMvc);
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
    void changeCategoryTypeTestShouldReturn400() throws Exception {
        JSONObject jsonObject = testingUtils.createImportPostingWithNested();
        testingUtils.postImport(jsonObject);
        jsonObject.getJSONArray("items").getJSONObject(0).put("type", "OFFER");
        jsonObject.getJSONArray("items").getJSONObject(0).getJSONArray("children").getJSONObject(0)
                .put("type", "CATEGORY");
        testingUtils.expectValidationFailed(testingUtils.postImport(jsonObject));
        mockMvc.perform(get("/nodes/" + testingUtils.getFirstImportObjectId())).andExpect(jsonPath("$.type")
                .value("CATEGORY"));
    }

    @Test
    void tryToSwitchTypeShouldReturnError() throws Exception {
        JSONObject jsonObject = testingUtils.createImportPostingJSON();
        testingUtils.postImport(jsonObject);
        jsonObject.getJSONArray("items").getJSONObject(0).put("type", "OFFER");
        testingUtils.expectValidationFailed(testingUtils.postImport(jsonObject));
    }


    @Test
    void postCorrectImportShouldReturn200() throws Exception {
        testingUtils.postImport(testingUtils.createImportPostingJSON()).andExpect(status().isOk());
    }

    @Test
    void postWithNullName() throws Exception {
        JSONObject jsonObject = testingUtils.createImportPostingJSON();
        jsonObject.getJSONArray("items").getJSONObject(0).put("name", null);
        testingUtils.expectValidationFailed(testingUtils.postImport(jsonObject));
    }

    @Test
    void postOfferWithChildrenShouldReturnError() throws Exception {
        JSONObject jsonObject = testingUtils.createImportPostingWithNested();
        jsonObject.getJSONArray("items").getJSONObject(0).put("type", "OFFER");
        testingUtils.expectValidationFailed(testingUtils.postImport(jsonObject));
    }

    @Test
    void postOfferWithoutPriceShouldReturnError() throws Exception {
        JSONObject jsonObject = testingUtils.createImportPostingWithNested();
        jsonObject.getJSONArray("items").getJSONObject(0).getJSONArray("children").getJSONObject(0)
                .remove("price");
        testingUtils.expectValidationFailed(testingUtils.postImport(jsonObject));
    }

    @Test
    void postCorrectOFFERWithNoParentShouldReturnOk() throws Exception {
        JSONObject offer = testingUtils.createNestedImportJSONObject();
        offer.remove("parentId");
        JSONObject postJSONObject = new JSONObject();
        JSONArray items = new JSONArray();
        items.put(offer);
        postJSONObject.put("items", items);
        postJSONObject.put("updateDate", "2022-02-01T12:00:00.000Z");
        testingUtils.postImport(postJSONObject).andExpect(status().isOk());
        mockMvc.perform(get("/nodes/" + testingUtils.getNestedImportObjectId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parentId").value(IsNull.nullValue()));
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
        testingUtils.postImport(testingUtils.createImportPostingJSON());
        mockMvc.perform(get("/nodes/" + testingUtils.getFirstImportObjectId())).andExpect(status().isOk());
    }

    @Test
    void postNodeWithNull() throws Exception {
        JSONObject jsonObject = testingUtils.createImportPostingJSON();
        jsonObject.put("parentId", null);
        testingUtils.postImport(jsonObject);
        mockMvc.perform(get("/nodes/" + testingUtils.getFirstImportObjectId())).andExpect(status().isOk());
    }

    @Test
    void testReturningChildren() throws Exception {
        JSONObject jsonObject = testingUtils.createImportPostingJSON();
        testingUtils.postImport(jsonObject);
        mockMvc.perform(get("/nodes/" + testingUtils.getFirstImportObjectId()))
                .andExpect(jsonPath("$.children").value(IsNull.nullValue()));
    }

    @Test
    void testChildrenCorrectness() throws Exception {
        testingUtils.postImport(testingUtils.createImportPostingWithNested());
        String childrenJSONPath = "$.children";
        mockMvc.perform(get("/nodes/" + testingUtils.getFirstImportObjectId()))
                .andExpect(jsonPath(childrenJSONPath).isArray()).andExpect(jsonPath(childrenJSONPath).isNotEmpty());
    }

    @Test
    void nullChildShouldReturnNull() throws Exception {
        JSONObject jsonObject = testingUtils.createImportPostingJSON();
        testingUtils.postImport(jsonObject);
        mockMvc.perform(get("/nodes/" + testingUtils.getFirstImportObjectId()))
                .andExpect(jsonPath("$.children").value(IsNull.nullValue()));
    }

    @Test
    void priceSavingTest() throws Exception {
        JSONObject jsonObject = testingUtils.createImportPostingWithNested();
        jsonObject.getJSONArray("items").getJSONObject(0).getJSONArray("children").getJSONObject(0)
                .put("price", 100000);
        testingUtils.postImport(jsonObject);
        mockMvc.perform(get("/nodes/" + testingUtils.getFirstImportObjectId()))
                .andExpect(jsonPath("$.children[0].price").value(100000));
    }

    @Test
    void categoryPriceAVGComputingTest() throws Exception {
        JSONObject jsonObject = testingUtils.createImportPostingWithNested();
        jsonObject.getJSONArray("items").getJSONObject(0).getJSONArray("children").getJSONObject(0)
                .put("price", 100000);
        testingUtils.postImport(jsonObject);
        mockMvc.perform(get("/nodes/" + testingUtils.getFirstImportObjectId()))
                .andExpect(jsonPath("$.price").value(100000));
    }

    @Test
    void updateTimeTest() throws Exception {
        JSONObject jsonObject = testingUtils.createImportPostingWithNested();
        testingUtils.postImport(jsonObject);
        mockMvc.perform(get("/nodes/" + testingUtils.getFirstImportObjectId()))
                .andExpect(jsonPath("$.date").value(jsonObject.getString("updateDate")));
    }

    @Test
    void recursivePriceAVGTest() throws Exception {
        JSONObject jsonObject = testingUtils.createImportPostingWithNested();
        JSONObject rootItem = jsonObject.getJSONArray("items").getJSONObject(0);
        JSONObject nestedObject = testingUtils.createNestedImportJSONObject();
        nestedObject.put("price", 1000);
        JSONObject secondNested = testingUtils.createNestedImportJSONObject();
        secondNested.put("id", "12312312321");
        secondNested.put("parent", testingUtils.getFirstImportObjectId());
        secondNested.put("price", 2000);
        rootItem.getJSONArray("children").put(secondNested);
        rootItem.getJSONArray("children").put(nestedObject);
        testingUtils.postImport(jsonObject);
        mockMvc.perform(get("/nodes/" + testingUtils.getFirstImportObjectId()))
                .andExpect(jsonPath("$.price").value(1500));
    }

    @Test
    void recursiveTimeTest() throws Exception {
        JSONObject jsonObject = testingUtils.createImportPostingWithNested();
        JSONObject rootItem = jsonObject.getJSONArray("items").getJSONObject(0);
        JSONObject nestedObject = testingUtils.createNestedImportJSONObject();
        JSONObject nestedInNested = testingUtils.createNestedImportJSONObject();
        nestedInNested.put("id", "12312312321");
        nestedInNested.put("parent", nestedObject.get("id"));
        nestedObject.getJSONArray("children").put(nestedInNested);
        rootItem.getJSONArray("children").put(nestedObject);
        testingUtils.postImport(jsonObject);
        jsonObject.put("updateDate", "2022-02-01T15:00:00.000Z");
        testingUtils.postImport(jsonObject);
        mockMvc.perform(get("/nodes/" + testingUtils.getFirstImportObjectId()))
                .andExpect(jsonPath("$.date").value("2022-02-01T15:00:00.000Z"));
    }
}
