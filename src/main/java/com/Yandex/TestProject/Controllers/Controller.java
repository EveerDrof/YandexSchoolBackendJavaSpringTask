package com.Yandex.TestProject.Controllers;

import com.Yandex.TestProject.Entities.ShopUnit;
import com.Yandex.TestProject.Entities.ShopUnitStatisticUnit;
import com.Yandex.TestProject.Entities.ShopUnitType;
import com.Yandex.TestProject.ErrorResponseObject;
import com.Yandex.TestProject.LocalDateTimeAdapter;
import com.Yandex.TestProject.Services.ShopUnitImportService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;

@org.springframework.stereotype.Controller
public class Controller {
    private ShopUnitImportService shopUnitService;
    private DateTimeFormatter formatter;
    private Gson gson;

    @Autowired
    public void ImportsController(ShopUnitImportService shopUnitImportService) {
        this.shopUnitService = shopUnitImportService;
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.nnn'Z'").withZone(ZoneId.of("UTC"));
        gson = new GsonBuilder().setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
    }

    private JSONObject shopUnitToJSON(ShopUnit shopUnit, boolean withChildren) {
        String jsonInString = gson.toJson(shopUnit);
        JSONObject resultJSON = new JSONObject(jsonInString);
        resultJSON.remove("parent");
        ShopUnit parent = shopUnit.getParent();
        if (parent != null) {
            resultJSON.put("parentId", parent.getId());
        } else {
            resultJSON.put("parentId", JSONObject.NULL);
        }
        if (withChildren) {
            ArrayList<ShopUnit> shopUnits = shopUnitService.findAllByParentId(shopUnit.getId());
            JSONArray jsonArray = new JSONArray();
            shopUnits.forEach((unit) -> {
                jsonArray.put(shopUnitToJSON(unit, withChildren));
            });
            if (jsonArray.length() == 0) {
                resultJSON.put("children", JSONObject.NULL);
            } else {
                resultJSON.put("children", jsonArray);
            }
        }
        if (shopUnit.getType() == ShopUnitType.CATEGORY) {
            resultJSON.put("price", shopUnitService.computeAveragePriceInCategory(shopUnit.getId()));
        }
        resultJSON.put("date", shopUnit.getDate().format(formatter));
        return resultJSON;
    }


    private void stringToShopUnitAndSave(JSONObject item, LocalDateTime updateDate, ArrayList<ShopUnit> offers)
            throws Exception {
        boolean isCategory = false;
        if (item.has("type")) {
            isCategory = item.getString("type").equals(ShopUnitType.CATEGORY.toString());
        } else {
            throw new Exception();
        }
        String name = item.getString("name");
        String type = item.getString("type");
        String id = item.getString("id");
        Optional<ShopUnit> foundEntityOptional = shopUnitService.findById(id);
        if (foundEntityOptional.isPresent()) {
            ShopUnit shopUnit = foundEntityOptional.get();
            if (!shopUnit.getType().toString().equals(type)) {
                throw new Exception();
            }
        }
        ShopUnit parentUnit = null;
        if (!item.isNull("parentId")) {
            String parentId = item.getString("parentId");
            Optional<ShopUnit> findResult = shopUnitService.findById(parentId);
            if (!findResult.isEmpty()) {
                parentUnit = findResult.get();
            }
        }
        long price = 0;
        if (item.has("price")) {
            price = item.getInt("price");
            if (price < 0) {
                throw new Exception();
            }
        } else {
            if (!isCategory) {
                throw new Exception();
            }
        }
        ShopUnit shopUnit = new ShopUnit(id, name, ShopUnitType.valueOf(type), parentUnit, price, updateDate);
        shopUnitService.save(shopUnit);
        shopUnitService.saveShopStatisticUnit(new ShopUnitStatisticUnit(shopUnit));
        if (shopUnit.getParent() != null) {
            if (shopUnit.getType() == ShopUnitType.OFFER && shopUnit.getDate().isAfter(shopUnit.getParent().getDate())) {
                offers.add(shopUnit);
            }
        }
        if (item.has("children") && item.getJSONArray("children").length() > 0) {
            if (isCategory) {
                JSONArray children = item.getJSONArray("children");
                for (int k = 0; k < children.length(); k++) {
                    JSONObject childJSONObject = children.getJSONObject(k);
                    stringToShopUnitAndSave(childJSONObject, updateDate, offers);
                }
            } else {
                throw new Exception();
            }
        }
    }

    @PostMapping(path = "imports")
    ResponseEntity postImport(@RequestBody String body) {
        try {
            JSONObject jsonObject = new JSONObject(body);
            JSONArray items = jsonObject.getJSONArray("items");
            String updateDate = jsonObject.getString("updateDate");
            LocalDateTime date = LocalDateTime.parse(updateDate, formatter);
            ArrayList<ShopUnit> offers = new ArrayList<>();
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                stringToShopUnitAndSave(item, date, offers);
            }
            offers.forEach((unit) -> {
                shopUnitService.updateDateForAllParents(unit.getId(), unit.getDate());
            });
        } catch (Exception exception) {
            return new ResponseEntity(new ErrorResponseObject(HttpStatus.BAD_REQUEST, "Validation Failed"),
                    HttpStatus.BAD_REQUEST);
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

        return new ResponseEntity(shopUnitToJSON(result, true).toString(), HttpStatus.OK);
    }

    @GetMapping(path = "sales", produces = {MediaType.APPLICATION_JSON_VALUE})
    ResponseEntity getSales(@RequestParam String date) {
        LocalDateTime dateTime = LocalDateTime.parse(date.substring(0, date.length() - 1));
        ArrayList<ShopUnit> sales = shopUnitService.getSales(dateTime);
        JSONObject responseJSON = new JSONObject();
        JSONArray items = new JSONArray();
        sales.forEach((unit) -> {
            items.put(shopUnitToJSON(unit, false));
        });
        responseJSON.put("items", items);
        return new ResponseEntity(responseJSON.toString(), HttpStatus.OK);
    }

    @GetMapping(path = "node/{id}/statistic", produces = {MediaType.APPLICATION_JSON_VALUE})
    ResponseEntity getNodeStatistic(@PathVariable String id, @RequestParam String dateStart,
                                    @RequestParam String dateEnd) {
        LocalDateTime dateTimeStart = LocalDateTime.parse(dateStart.substring(0, dateStart.length() - 1));
        LocalDateTime dateTimeEnd = LocalDateTime.parse(dateEnd.substring(0, dateEnd.length() - 1));
        ArrayList<ShopUnitStatisticUnit> items = shopUnitService.findStatisticsAllByIdAndPeriod(id, dateTimeStart,
                dateTimeEnd);
        JSONObject responseJSON = new JSONObject();
        responseJSON.put("items", new JSONArray(gson.toJson(items)));
        return new ResponseEntity(responseJSON.toString(), HttpStatus.OK);
    }

    @DeleteMapping(path = "delete/{id}")
    ResponseEntity deleteNode(@PathVariable String id) {
        Optional<ShopUnit> shopUnitOptional = shopUnitService.findById(id);
        if (shopUnitOptional.isEmpty()) {
            return new ResponseEntity<>(new ErrorResponseObject(HttpStatus.NOT_FOUND, "Item not found"),
                    HttpStatus.NOT_FOUND);
        }
        ShopUnit shopUnit = shopUnitOptional.get();
        shopUnitService.deleteByIdRecursive(shopUnit);
        return new ResponseEntity(HttpStatus.OK);
    }
}
