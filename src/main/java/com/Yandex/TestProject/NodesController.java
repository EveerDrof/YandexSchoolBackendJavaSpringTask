package com.Yandex.TestProject;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;

@Controller
public class NodesController {
    @GetMapping(path = "nodes/{id}")
    ResponseEntity<HashMap<String, Object>> getNode(@PathVariable String id) {
        HashMap<String, Object> hashMap = new HashMap();
        hashMap.put("code", 404);
        hashMap.put("message", "Item not found");
        return new ResponseEntity<>(hashMap, HttpStatus.NOT_FOUND);
    }
}
