package com.Yandex.TestProject;

import jakarta.transaction.Transactional;
import org.hamcrest.core.IsNull;
import org.json.JSONArray;
import org.json.JSONException;
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
    private String nestedImportObjectId = "069cb8d7-bbdd-47d3-ad3f-82ef5c262df2";

    public TestProjectApplicationTests(@Autowired MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    private JSONObject createImportPostingJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONArray items = new JSONArray();
        JSONObject item = new JSONObject();
        item.put("type", "CATEGORY");
        item.put("name", "Товар");
        item.put("id", firstImportObjectId);
        item.put("parentId", "None");
        items.put(item);
        jsonObject.put("items", items);
        jsonObject.put("updateDate", "2022-02-01T12:00:00.000Z");
        return jsonObject;
    }

    private JSONObject createImportPostingWithNested() throws JSONException {
        JSONObject jsonObject = createImportPostingJSON();
        JSONObject nestedObject = createNestedImportJSONObject();
        JSONArray children = new JSONArray();
        children.put(nestedObject);
        jsonObject.getJSONArray("items").getJSONObject(0).put("children", children);
        return jsonObject;
    }

    private JSONObject createNestedImportJSONObject() throws JSONException {
        JSONObject nestedObject = createImportPostingJSON();
        nestedObject.put("parentId", firstImportObjectId);
        nestedObject.put("id", nestedImportObjectId);
        nestedObject.put("name", "aaaaaa");
        nestedObject.remove("items");
        nestedObject.remove("updateDate");
        nestedObject.put("type", "OFFER");
        nestedObject.put("children", new JSONArray());
        return nestedObject;
    }

    private ResultActions postImport(JSONObject jsonObject) throws Exception {

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
        postImport(createImportPostingJSON()).andExpect(status().isOk());

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
        postImport(createImportPostingJSON());
        mockMvc.perform(get("/nodes/" + firstImportObjectId)).andExpect(status().isOk());
    }

    @Test
    void postNodeWithNull() throws Exception {
        JSONObject jsonObject = createImportPostingJSON();
        jsonObject.put("parentId", null);
        postImport(jsonObject);
        mockMvc.perform(get("/nodes/" + firstImportObjectId)).andExpect(status().isOk());
    }

    @Test
    void testReturningChildren() throws Exception {
        JSONObject jsonObject = createImportPostingJSON();
        postImport(jsonObject);
        mockMvc.perform(get("/nodes/" + firstImportObjectId))
                .andExpect(jsonPath("$.children").value(IsNull.nullValue()));
    }

    @Test
    void testChildrenCorrectness() throws Exception {
        postImport(createImportPostingWithNested());
        String childrenJSONPath = "$.children";
        mockMvc.perform(get("/nodes/" + firstImportObjectId))
                .andExpect(jsonPath(childrenJSONPath).isArray()).andExpect(jsonPath(childrenJSONPath).isNotEmpty());
    }

    @Test
    void nullChildShouldReturnNull() throws Exception {
        JSONObject jsonObject = createImportPostingJSON();
        postImport(jsonObject);
        mockMvc.perform(get("/nodes/" + firstImportObjectId)).andExpect(jsonPath("$.children")
                .value(IsNull.nullValue()));
    }

    @Test
    void priceSavingTest() throws Exception {
        JSONObject jsonObject = createImportPostingWithNested();
        jsonObject.getJSONArray("items").getJSONObject(0).getJSONArray("children").getJSONObject(0)
                .put("price", 100000);
        postImport(jsonObject);
        mockMvc.perform(get("/nodes/" + firstImportObjectId))
                .andExpect(jsonPath("$.children[0].price").value(100000));
    }

    @Test
    void categoryPriceAVGComputingTest() throws Exception {
        JSONObject jsonObject = createImportPostingWithNested();
        jsonObject.getJSONArray("items").getJSONObject(0).getJSONArray("children").getJSONObject(0)
                .put("price", 100000);
        postImport(jsonObject);
        mockMvc.perform(get("/nodes/" + firstImportObjectId))
                .andExpect(jsonPath("$.price").value(100000));
    }

    @Test
    void updateTimeTest() throws Exception {
        JSONObject jsonObject = createImportPostingWithNested();
        postImport(jsonObject);
        mockMvc.perform(get("/nodes/" + firstImportObjectId))
                .andExpect(jsonPath("$.date").value(jsonObject.getString("updateDate")));
    }

    @Test
    void recursivePriceAVGTest() throws Exception {
        JSONObject jsonObject = createImportPostingWithNested();
        JSONObject rootItem = jsonObject.getJSONArray("items").getJSONObject(0);
        JSONObject nestedObject = createNestedImportJSONObject();
        nestedObject.put("price", 1000);
        JSONObject nestedInNested = createNestedImportJSONObject();
        nestedInNested.put("id", "12312312321");
        nestedInNested.put("parent", nestedObject.get("id"));
        nestedInNested.put("price", 2000);
        nestedObject.getJSONArray("children").put(nestedInNested);
        rootItem.getJSONArray("children").put(nestedObject);
        postImport(jsonObject);
        mockMvc.perform(get("/nodes/" + firstImportObjectId))
                .andExpect(jsonPath("$.price").value(1500));
    }

    @Test
    void recursiveTimeTest() throws Exception {
        JSONObject jsonObject = createImportPostingWithNested();
        JSONObject rootItem = jsonObject.getJSONArray("items").getJSONObject(0);
        JSONObject nestedObject = createNestedImportJSONObject();
        JSONObject nestedInNested = createNestedImportJSONObject();
        nestedInNested.put("id", "12312312321");
        nestedInNested.put("parent", nestedObject.get("id"));
        nestedObject.getJSONArray("children").put(nestedInNested);
        rootItem.getJSONArray("children").put(nestedObject);
        postImport(jsonObject);
        jsonObject.put("updateDate", "2022-02-01T15:00:00.000Z");
        postImport(jsonObject);
        mockMvc.perform(get("/nodes/" + firstImportObjectId))
                .andExpect(jsonPath("$.date").value("2022-02-01T15:00:00.000Z"));
    }

    @Test
    void salesItemsTest() throws Exception {
        JSONObject jsonObject = createImportPostingWithNested();
        postImport(jsonObject);
        mockMvc.perform(get("/sales").param("date", jsonObject.getString("updateDate"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }
}
