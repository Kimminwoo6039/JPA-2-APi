package com.jpabook.www.service;

import com.jpabook.www.domain.item.Book;
import com.jpabook.www.domain.item.Item;
import com.jpabook.www.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional // readonly =false 값 저장하는거는 true 면 안되고 읽기만 true
    public void saveItem(Item item) {
        itemRepository.save(item);
    }

    @Transactional
    public Item updateItem(Long itemId, Book param) { // 변경 감지를 사용한 저장법.. 이걸 추천!!!!!
        Item findItem = itemRepository.findOne(itemId);
        findItem.setPrice(param.getPrice());
        findItem.setName(param.getName());
        findItem.setStockQuantity(findItem.getStockQuantity());
        return findItem;
    }

    public List<Item> findItems() {
        return itemRepository.findAll();
    }

    public Item findOne(Long itemId) {
        return itemRepository.findOne(itemId);
    }
}
