package com.Yandex.TestProject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TestingUtils {

    private final String firstImportObjectId = "069cb8d7-bbdd-47d3-ad8f-82ef4c269df1";
    private final MockMvc mockMvc;
    private String nestedImportObjectId = "069cb8d7-bbdd-47d3-ad3f-82ef5c262df2";

    public TestingUtils(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    public String getFirstImportObjectId() {
        return firstImportObjectId;
    }

    public String getNestedImportObjectId() {
        return nestedImportObjectId;
    }

    public ResultActions postImport(JSONObject jsonObject) throws Exception {
        return mockMvc.perform(post("/imports").contentType(MediaType.APPLICATION_JSON)
                .content(jsonObject.toString()));
    }

    public JSONObject createImportPostingJSON() throws JSONException {
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

    public JSONObject createImportPostingWithNested() throws JSONException {
        JSONObject jsonObject = createImportPostingJSON();
        JSONObject nestedObject = createNestedImportJSONObject();
        JSONArray children = new JSONArray();
        children.put(nestedObject);
        jsonObject.getJSONArray("items").getJSONObject(0).put("children", children);
        return jsonObject;
    }

    public JSONObject createNestedImportJSONObject() throws JSONException {
        JSONObject nestedObject = createImportPostingJSON();
        nestedObject.put("parentId", firstImportObjectId);
        nestedObject.put("id", nestedImportObjectId);
        nestedObject.put("name", "aaaaaa");
        nestedObject.remove("items");
        nestedObject.remove("updateDate");
        nestedObject.put("type", "OFFER");
        nestedObject.put("children", new JSONArray());
        nestedObject.put("price", 0);
        return nestedObject;
    }

    public void expectValidationFailed(ResultActions resultActions) throws Exception {
        resultActions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("Validation Failed"));
    }
}
