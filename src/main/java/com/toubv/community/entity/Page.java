package com.toubv.community.entity;

public class Page {
    //当前页码
    private int current = 1;
    //每页能展示的帖子数量上限
    private int limit = 10;
    //查询的总记录数
    private int rows;
    //url
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if(current >= 1)
            this.current = current;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if(limit >= 1 && limit <= 100)
            this.limit = limit;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if(rows >= 0)
            this.rows = rows;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getOffset(){
        return (current - 1) * limit;
    }

    public int getTotal(){
        return (rows + limit - 1) / limit;
    }

    public int getFrom(){
        return Math.max(1, current - 2);
    }

    public int getTo(){
        return Math.min(current + 2, getTotal());
    }
}
