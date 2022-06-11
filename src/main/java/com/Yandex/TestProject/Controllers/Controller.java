package com.Yandex.TestProject.Controllers;

import com.Yandex.TestProject.Entities.ShopUnit;
import com.Yandex.TestProject.Entities.ShopUnitType;
import com.Yandex.TestProject.ErrorResponseObject;
import com.Yandex.TestProject.Services.ShopUnitImportService;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.Optional;

@org.springframework.stereotype.Controller
public class Controller {
    private ShopUnitImportService shopUnitService;

    @Autowired
    public void ImportsController(ShopUnitImportService shopUnitImportService) {
        this.shopUnitService = shopUnitImportService;
    }

    private JSONObject shopUnitToJSON(ShopUnit shopUnit) {
        Gson gson = new Gson();
        String jsonInString = gson.toJson(shopUnit);
        JSONObject resultJSON = new JSONObject(jsonInString);
        ArrayList<ShopUnit> shopUnits = shopUnitService.findAllByParentId(shopUnit.getId());
        JSONArray jsonArray = new JSONArray();
        shopUnits.forEach((unit) -> {
            jsonArray.put(shopUnitToJSON(unit));
        });
        resultJSON.put("children", jsonArray);
        return resultJSON;
    }

    private void stringToShopUnitAndSave(JSONObject item) {
        String name = item.getString("name");
        String type = item.getString("type");
//                long type = item.getString("price");
        String id = item.getString("id");
        ShopUnit parentUnit = null;
        if (!item.isNull("parentId")) {
            String parentId = item.getString("parentId");
            Optional<ShopUnit> findResult = shopUnitService.findById(parentId);
            if (!findResult.isEmpty()) {
                parentUnit = findResult.get();
            }
        }
        ShopUnit shopUnit = new ShopUnit(id, name, ShopUnitType.valueOf(type), parentUnit);
        shopUnitService.save(shopUnit);
        if (item.has("children")) {
            JSONArray children = item.getJSONArray("children");
            for (int k = 0; k < children.length(); k++) {
                JSONObject childJSONObject = children.getJSONObject(k);
                stringToShopUnitAndSave(childJSONObject);
            }
        }
    }

    @PostMapping(path = "imports")
    ResponseEntity postImport(@RequestBody String body) {
        if (body.isEmpty()) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        try {
            JSONObject jsonObject = new JSONObject(body);
            JSONArray items = jsonObject.getJSONArray("items");
            String updateDate = jsonObject.getString("updateDate");
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                stringToShopUnitAndSave(item);
            }
        } catch (Exception exception) {
            return new ResponseEntity("Validation Failed", HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping(path = "nodes/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    ResponseEntity getNode(@PathVariable String id) {
        Optional<ShopUnit> findResult = shopUnitService.findById(id);
        if (findResult.isEmpty()) {
            return new ResponseEntity(new ErrorResponseObject(HttpStatus.NOT_FOUND, "Item not found"),
                    HttpStatus.NOT_FOUND);
        }
        ShopUnit result = findResult.get();

        return new ResponseEntity(shopUnitToJSON(result).toString(), HttpStatus.OK);
    }
}
