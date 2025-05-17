package ru.otus.java.basic.http.server.application;

import java.util.List;

public interface ItemsDatabaseProvider {

    public List<Item> getAllItems();

    public Item getItemById(Long itemId);

    //public void addNewItem(Item item);

}
