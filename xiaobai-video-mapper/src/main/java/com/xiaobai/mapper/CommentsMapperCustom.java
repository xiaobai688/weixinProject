package com.xiaobai.mapper;

import com.xiaobai.pojo.Comments;
import com.xiaobai.pojo.vo.CommentsVO;
import com.xiaobai.utils.MyMapper;

import java.util.List;



public interface CommentsMapperCustom extends MyMapper<Comments> {
	
	public List<CommentsVO> queryComments(String videoId);

	/**
	 * @Description: 对评论喜欢的数量进行累加
	 */
	public void addCommentLikeCount(String commentId);
	/**
	 * @Description: 对评论喜欢的数量进行累减
	 */
	public void reduceCommentLikeCount(String commentId);
}