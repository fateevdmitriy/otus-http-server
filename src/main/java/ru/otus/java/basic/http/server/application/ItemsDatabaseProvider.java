package ru.otus.java.basic.http.server.application;

import java.util.List;

public interface ItemsDatabaseProvider {

    public List<Item> getAllItems();

    public Item getItemById(Long itemId);

    public int addItem(Item item);

    public int deleteItemById(Long itemId);

    public int updateItem(Item updatedItem);
}
