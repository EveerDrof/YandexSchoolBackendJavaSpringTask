package com.Yandex.TestProject.Controllers;

import com.Yandex.TestProject.Entities.ShopUnit;
import com.Yandex.TestProject.Entities.ShopUnitStatisticUnit;
import com.Yandex.TestProject.Entities.ShopUnitTemplate;
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
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Optional;

@org.springframework.stereotype.Controller
public class Controller {
    private ShopUnitImportService shopUnitService;
    private DateTimeFormatter formatter;
    private Gson gson;
    private ResponseEntity notFoundResponseEntity;
    private ResponseEntity validationsFailedResponseEntity;

    @Autowired
    public void ImportsController(ShopUnitImportService shopUnitImportService) {
        this.shopUnitService = shopUnitImportService;
        formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.000'Z'").withZone(ZoneId.of("UTC"))
                .withResolverStyle(ResolverStyle.LENIENT);
        gson = new GsonBuilder().setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
        validationsFailedResponseEntity = new ResponseEntity(new ErrorResponseObject(HttpStatus.BAD_REQUEST,
                "Validation Failed"), HttpStatus.BAD_REQUEST);
        notFoundResponseEntity = new ResponseEntity(new ErrorResponseObject(HttpStatus.NOT_FOUND,
                "Item not found"), HttpStatus.NOT_FOUND);
    }

    private JSONObject shopUnitToJSON(ShopUnitTemplate shopUnit, String id, boolean withChildren) {
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
            ArrayList<ShopUnit> shopUnits = shopUnitService.findAllByParentId(id);
            JSONArray jsonArray = new JSONArray();
            shopUnits.forEach((unit) -> jsonArray.put(shopUnitToJSON(unit, unit.getId(), true)));
            if (jsonArray.length() == 0) {
                resultJSON.put("children", JSONObject.NULL);
            } else {
                resultJSON.put("children", jsonArray);
            }
        }
        long price = shopUnit.getPrice();
        if (price < 0) {
            resultJSON.put("price", JSONObject.NULL);
        } else {
            resultJSON.put("price", price);
        }
        resultJSON.put("date", shopUnit.getDate().format(formatter));
        return resultJSON;
    }

    private JSONObject shopUnitToJSON(ShopUnitStatisticUnit shopUnit, String id, boolean withChildren) {
        JSONObject jsonObject = shopUnitToJSON((ShopUnitTemplate) shopUnit, id, withChildren);
        jsonObject.remove("children");
        return jsonObject;
    }

    private void stringToShopUnitAndSave(JSONObject item, LocalDateTime updateDate, ArrayList<ShopUnit> offers,
                                         ArrayList<String> shopUnitsIdsForStatistics)
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
        shopUnit = shopUnitService.save(shopUnit);
        if (isCategory) {
            shopUnitsIdsForStatistics.add(shopUnit.getId());
        }
        if (isCategory) {
            replaceAveragePriceRecursive(shopUnit);
        } else {
            if (shopUnit.getParent() != null) {
                replaceAveragePriceRecursive(shopUnit.getParent());
            }
        }
        if (!isCategory) {
            shopUnitService.saveShopStatisticUnit(new ShopUnitStatisticUnit(shopUnit));
        }
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
                    stringToShopUnitAndSave(childJSONObject, updateDate, offers, shopUnitsIdsForStatistics);
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
            ArrayList<String> shopUnitsIdsForStatistics = new ArrayList<>();
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                stringToShopUnitAndSave(item, date, offers, shopUnitsIdsForStatistics);
            }
            offers.forEach((unit) -> {
                shopUnitService.updateDateForAllParents(unit.getId(), unit.getDate());
            });
            shopUnitsIdsForStatistics.forEach((id) -> {
                ShopUnit categoryShopUnit = shopUnitService.findById(id).get();
                shopUnitService.saveShopStatisticUnit(new ShopUnitStatisticUnit(categoryShopUnit));
            });
        } catch (Exception exception) {
            return validationsFailedResponseEntity;
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping(path = "nodes/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    ResponseEntity getNode(@PathVariable String id) {
        Optional<ShopUnit> findResult = shopUnitService.findById(id);
        if (findResult.isEmpty()) {
            return notFoundResponseEntity;
        }
        ShopUnit result = findResult.get();

        return new ResponseEntity(shopUnitToJSON(result, result.getId(), true).toString(), HttpStatus.OK);
    }

    @GetMapping(path = "sales", produces = {MediaType.APPLICATION_JSON_VALUE})
    ResponseEntity getSales(@RequestParam String date) {
        LocalDateTime dateTime;
        try {
            dateTime = LocalDateTime.parse(date, formatter);
        } catch (Exception ex) {
            return validationsFailedResponseEntity;
        }
        ArrayList<ShopUnit> sales = shopUnitService.getSales(dateTime);
        JSONObject responseJSON = new JSONObject();
        JSONArray items = new JSONArray();
        sales.forEach((unit) -> {
            items.put(shopUnitToJSON(unit, unit.getId(), true));
        });
        responseJSON.put("items", items);
        return new ResponseEntity(responseJSON.toString(), HttpStatus.OK);
    }

    @GetMapping(path = "node/{id}/statistic", produces = {MediaType.APPLICATION_JSON_VALUE})
    ResponseEntity getNodeStatistic(@PathVariable String id, @RequestParam String dateStart,
                                    @RequestParam String dateEnd) {
        LocalDateTime dateTimeStart;
        LocalDateTime dateTimeEnd;
        try {
            dateTimeStart = LocalDateTime.parse(dateStart, formatter);
            dateTimeEnd = LocalDateTime.parse(dateEnd, formatter);
        } catch (Exception ex) {
            return validationsFailedResponseEntity;
        }
        Optional<ShopUnit> shopUnitOptional = shopUnitService.findById(id);
        if (shopUnitOptional.isEmpty()) {
            return notFoundResponseEntity;
        }
        ShopUnit shopUnit = shopUnitOptional.get();
        ArrayList<ShopUnitStatisticUnit> items = shopUnitService.findStatisticsAllByIdAndPeriod(shopUnit, dateTimeStart,
                dateTimeEnd);
        JSONObject responseJSON = new JSONObject();
        JSONArray itemsJSONArray = new JSONArray();
        items.forEach((statisticUnit) -> itemsJSONArray.put(shopUnitToJSON(statisticUnit, statisticUnit.getId(),
                false)));
        responseJSON.put("items", itemsJSONArray);
        return new ResponseEntity(responseJSON.toString(), HttpStatus.OK);
    }

    @DeleteMapping(path = "delete/{id}")
    ResponseEntity deleteNode(@PathVariable String id) {
        Optional<ShopUnit> shopUnitOptional = shopUnitService.findById(id);
        if (shopUnitOptional.isEmpty()) {
            return notFoundResponseEntity;
        }
        ShopUnit shopUnit = shopUnitOptional.get();
        shopUnitService.deleteByIdRecursive(shopUnit);
        replaceAveragePriceRecursive(shopUnit.getParent());
        if (shopUnit.getParent() != null) {
            replaceAveragePriceRecursive(new ShopUnitStatisticUnit(shopUnit.getParent()));
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    private void replaceAveragePriceRecursive(Optional<ShopUnitTemplate> shopUnitTemplateOptional) {
        if (shopUnitTemplateOptional.isEmpty()) {
            return;
        }
        replaceAveragePriceRecursive(shopUnitTemplateOptional.get());
    }

    private void replaceAveragePriceRecursive(ShopUnitTemplate shopUnitTemplate) {
        if (shopUnitTemplate == null) {
            return;
        }

        long price = -1;
        if (shopUnitService.findAllByParentId(shopUnitTemplate.getId()).size() > 0) {
            price = shopUnitService.computeAveragePriceInCategory(shopUnitTemplate.getId());
        }
        shopUnitTemplate.setPrice(price);
        if (shopUnitTemplate instanceof ShopUnit) {
            shopUnitService.save((ShopUnit) shopUnitTemplate);
        } else {
            shopUnitService.saveShopStatisticUnit((ShopUnitStatisticUnit) shopUnitTemplate);
        }
        replaceAveragePriceRecursive(shopUnitTemplate.getParent());
    }
}
