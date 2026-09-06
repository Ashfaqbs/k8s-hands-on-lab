package com.example.demoapp.item;

import com.example.demoapp.item.ItemDtos.ItemRequest;
import com.example.demoapp.item.ItemDtos.ItemResponse;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public List<ItemResponse> findAll() {
        return itemRepository.findAll().stream()
                .map(ItemResponse::from)
                .toList();
    }

    public ItemResponse create(ItemRequest request) {
        Item saved = itemRepository.save(new Item(request.name(), request.quantity()));
        return ItemResponse.from(saved);
    }
}
