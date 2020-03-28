package com.xiaobai.service;

import com.xiaobai.pojo.Comments;
import com.xiaobai.pojo.Videos;
import com.xiaobai.pojo.vo.CommentsVO;
import com.xiaobai.utils.PagedResult;

import java.util.List;


public interface VideoService {
	
	/**
	 * @Description: 保存视频
	 */
	public String saveVideo(Videos video);
	
	/**
	 * @Description: 修改视频的封面
	 */
	public void updateVideo(String videoId, String coverPath);
	
	/**
	 * @Description: 分页查询视频列表
	 */
	public PagedResult getAllVideos(Videos video, Integer isSaveRecord,
									Integer page, Integer pageSize);
	
	/**
	 * @Description: 查询我喜欢的视频列表
	 */
	public PagedResult queryMyLikeVideos(String userId, Integer page, Integer pageSize);
	
	/**
	 * @Description: 查询我关注的人的视频列表
	 */
	public PagedResult queryMyFollowVideos(String userId, Integer page, Integer pageSize);
	
	/**
	 * @Description: 获取热搜词列表
	 */
	public List<String> getHotwords();


	/**
	 * @Description: 获取视频路径列表
	 */
	public List<String> getVideosPath();

	/**
	 * @Description: 用户喜欢/点赞视频
	 */
	public void userLikeVideo(String userId, String videoId, String videoCreaterId);
	
	/**
	 * @Description: 用户不喜欢/取消点赞视频
	 */
	public void userUnLikeVideo(String userId, String videoId, String videoCreaterId);
	
	/**
	 * @Description: 用户留言
	 */
	public void saveComment(Comments comment);

	/**
	 * 查询视频评论数
	 */
	public int commentCount(String videoId);

	/**
	 * @Description: 留言
	 */
	public List<CommentsVO> getAllComments(String videoId);

	/**
	 * @Description: 用户喜欢/点赞评论
	 */
	public void userLikeComment(String userId, String commentId, String commentCreaterId);

	/**
	 * @Description: 用户不喜欢/取消点赞评论
	 */
	public void userUnLikeComment(String userId, String commentId, String commentCreaterId);
}


