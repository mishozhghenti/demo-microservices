package com.flatrock.inventoryservice.controller;

import com.flatrock.inventoryservice.dto.InventoryResponse;
import com.flatrock.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> isInStock(@RequestParam("skuCodes") List<String> skuCodes) {
        return inventoryService.isInStock(skuCodes);
    }
}
