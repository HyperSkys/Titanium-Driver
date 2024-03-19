package dev.tactiletech.titaniumdriver.database;

import dev.tactiletech.titaniumdriver.database.exceptions.collection.FailedToGetAllCollectionsException;

@SuppressWarnings("all")
public interface Database {
    Collection[] getCollections() throws FailedToGetAllCollectionsException;
    Collection getCollection(String name);
    Collection createCollection(String name);
    void deleteCollection(String name);
    void delete();
    String getName();
}
