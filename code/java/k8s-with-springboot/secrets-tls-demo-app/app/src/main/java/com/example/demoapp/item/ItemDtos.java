package com.example.demoapp.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public final class ItemDtos {

    private ItemDtos() {
    }

    public record ItemRequest(
            @NotBlank String name,
            @NotNull @PositiveOrZero Integer quantity) {
    }

    public record ItemResponse(Long id, String name, Integer quantity) {
        static ItemResponse from(Item item) {
            return new ItemResponse(item.getId(), item.getName(), item.getQuantity());
        }
    }
}
