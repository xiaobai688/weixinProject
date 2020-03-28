package com.xiaobai.mapper;

import java.util.List;

import com.xiaobai.pojo.Videos;
import com.xiaobai.pojo.vo.VideosVO;
import com.xiaobai.utils.MyMapper;
import org.apache.ibatis.annotations.Param;


public interface VideosMapperCustom extends MyMapper<Videos> {
	
	/**
	 * @Description: 条件查询所有视频列表
	 */
	public List<VideosVO> queryAllVideos(@Param("videoDesc") String videoDesc,
										 @Param("userId") String userId);
	
	/**
	 * @Description: 查询关注的视频
	 */
	public List<VideosVO> queryMyFollowVideos(String userId);
	
	/**
	 * @Description: 查询点赞视频
	 */
	public List<VideosVO> queryMyLikeVideos(@Param("userId") String userId);
	
	/**
	 * @Description: 对视频喜欢的数量进行累加
	 */
	public void addVideoLikeCount(String videoId);
	
	/**
	 * @Description: 对视频喜欢的数量进行累减
	 */
	public void reduceVideoLikeCount(String videoId);
}