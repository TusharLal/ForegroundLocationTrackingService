package com.example.tusharlal.gpstrackingdemo;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface LocationInfoDao {

    @Query("SELECT * FROM location_info")
    List<LocationInfo> getAll();

    @Insert
    void insertInfo(LocationInfo info);

    @Query("DELETE FROM  location_info")
    void deleteAll();
}
