package dev.akuniutka.bank.api.controller;

import dev.akuniutka.bank.api.dto.OperationDto;
import dev.akuniutka.bank.api.service.Operations;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
public class OperationController {
    private final Operations operations;

    public OperationController(Operations operations) {
        this.operations = operations;
    }

    @GetMapping("/getOperationList")
    @Operation(summary = "Get the list of operations for a selected user (all or foe specified period)")
    public List<OperationDto> getOperationList(
            @RequestParam(required = false) Long userId,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date dateFrom,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateFrom,
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date dateTo
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateTo
    ) {
        return operations.getList(userId, dateFrom, dateTo);
//        return operations.getList(userId, null, null);
    }
}
