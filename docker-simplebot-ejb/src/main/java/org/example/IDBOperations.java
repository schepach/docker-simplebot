package org.example;

import java.util.List;

public interface IDBOperations {

    List<String> selectDataFromDB();

    String selectDataFromDB(int userId);

    void createTableToDB();

    void insertDataToDB(int userId, String name);

}