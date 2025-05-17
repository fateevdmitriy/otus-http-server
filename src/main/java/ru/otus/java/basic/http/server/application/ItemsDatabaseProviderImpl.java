package ru.otus.java.basic.http.server.application;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.otus.java.basic.http.server.Dispatcher;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ItemsDatabaseProviderImpl implements ItemsDatabaseProvider {
    //private List<Item> items;
    private final Connection connection;
    private static final Logger logger = LogManager.getLogger(ItemsDatabaseProviderImpl.class);

    private static final String ITEMS_SELECT_ALL_QUERY = "select * from items";
    private static final String ITEMS_SELECT_BY_ID_QUERY = "select * from items where id = ?";
    private static final String DATABASE_URL = "jdbc:postgresql://localhost:5432/otus-db";

    public ItemsDatabaseProviderImpl() {
        try {
            logger.info("-> ItemsDatabaseProviderImpl is started.");
            this.connection = DriverManager.getConnection(DATABASE_URL, "admin", "password");
        } catch (SQLException e) {
            throw new RuntimeException("-> "+ e);
        }
    }

    private Item mapResultSetToItem(ResultSet rs) {
        try {
            Long id = rs.getLong(1);
            String title = rs.getString(2);
            BigDecimal price = rs.getBigDecimal(3);
            Integer weight = rs.getInt(4);
            return new Item(id, title, price, weight);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Item> getAllItems() {
        List<Item> allItems = new CopyOnWriteArrayList<>();

        try (Statement statement = connection.createStatement()) {
            try (ResultSet rs = statement.executeQuery(ITEMS_SELECT_ALL_QUERY)) {
                while (rs.next()) {
                    allItems.add(mapResultSetToItem(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return allItems;
    }

    @Override
    public Item getItemById(Long itemId) {
        try (PreparedStatement ps = connection.prepareStatement(ITEMS_SELECT_BY_ID_QUERY)) {
            ps.setLong(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToItem(rs);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /*
    @Override
    public void addNewItem(Item item) {
        items.add(item);
    }
     */

}
