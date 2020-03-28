package com.xiaobai.mapper;

import com.xiaobai.pojo.SearchRecords;
import com.xiaobai.utils.MyMapper;

import java.util.List;



public interface SearchRecordsMapper extends MyMapper<SearchRecords> {
	
	public List<String> getHotwords();
}