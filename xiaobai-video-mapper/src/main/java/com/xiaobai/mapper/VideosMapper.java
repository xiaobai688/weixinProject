package com.xiaobai.mapper;


import com.xiaobai.pojo.Videos;
import com.xiaobai.utils.MyMapper;

import java.util.List;

public interface VideosMapper extends MyMapper<Videos> {

    /**
     *
     * @return 获取视频路径
     */
    public List<String> getVideosPath();
}