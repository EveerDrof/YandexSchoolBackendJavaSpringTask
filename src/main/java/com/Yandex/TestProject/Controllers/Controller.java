package com.Yandex.TestProject.Controllers;

import com.Yandex.TestProject.Entities.ShopUnit;
import com.Yandex.TestProject.Entities.ShopUnitType;
import com.Yandex.TestProject.ErrorResponseObject;
import com.Yandex.TestProject.Services.ShopUnitImportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;

@org.springframework.stereotype.Controller
public class Controller {
    private ShopUnitImportService shopUnitService;

    @Autowired
    public void ImportsController(ShopUnitImportService shopUnitImportService) {
        this.shopUnitService = shopUnitImportService;
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
            final ObjectMapper mapper = new ObjectMapper();
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                String name = item.getString("name");
                String type = item.getString("type");
                String id = item.getString("id");
                String parentId = item.getString("parentId");
                ShopUnit parentUnit = null;
                if (!parentId.isEmpty() && !parentId.equals("null")) {
                    parentUnit = shopUnitService.findById(parentId).get();
                }
                ShopUnit shopUnit = new ShopUnit(id, name, ShopUnitType.valueOf(type), parentUnit);
                shopUnitService.save(shopUnit);
            }
        } catch (Exception exception) {
            return new ResponseEntity("Validation Failed", HttpStatus.BAD_REQUEST);
        }
//        ArrayList<Object> items = (ArrayList<HashMap<String,Object>>) body.get("items");
////                mapper.convertValue(body.get("items"), new TypeReference<>() {
////        });
//        for (Object item : items) {
//            mapper.convertValue(body.get("items"),ShopUnit.class)
//            shopUnitService.save(item);
//        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping(path = "nodes/{id}")
    ResponseEntity getNode(@PathVariable String id) {
        Optional<ShopUnit> findResult = shopUnitService.findById(id);
        if (findResult.isEmpty()) {
            return new ResponseEntity(new ErrorResponseObject(HttpStatus.NOT_FOUND, "Item not found"),
                    HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity(findResult.get(), HttpStatus.OK);
//        HashMap<String, Object> hashMap = new HashMap();
//        hashMap.put("code", 404);
//        hashMap.put("message", "Item not found");
//        return new ResponseEntity<>(hashMap, HttpStatus.NOT_FOUND);
    }
}
