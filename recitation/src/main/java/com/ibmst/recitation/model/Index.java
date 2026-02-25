package com.ibmst.recitation.model;

import java.util.List;

public class Index {
    public Index(List<PageIndex> pageIndexList) {
        this.pageIndexList = pageIndexList;
    }

    public List<PageIndex> getPageIndexList() {
        return pageIndexList;
    }

    public void setPageIndexList(List<PageIndex> pageIndexList) {
        this.pageIndexList = pageIndexList;
    }

    public void add(PageIndex item) {
        this.pageIndexList.add(item);
    }

    List<PageIndex> pageIndexList;
}