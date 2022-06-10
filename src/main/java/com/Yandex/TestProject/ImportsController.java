package com.Yandex.TestProject;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class ImportsController {
    @PostMapping(path = "imports")
    ResponseEntity postImport(@RequestBody @Valid ShopUnitImportRequest shopUnitImportRequest) {
        if (shopUnitImportRequest == null) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity(HttpStatus.OK);
        }
    }
}
