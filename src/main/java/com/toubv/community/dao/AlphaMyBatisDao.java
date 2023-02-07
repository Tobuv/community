package com.toubv.community.dao;

import org.springframework.stereotype.Repository;

@Repository
public class AlphaMyBatisDao implements AlphaDao{
    @Override
    public String select() {
        return "MyBatis";
    }
}
