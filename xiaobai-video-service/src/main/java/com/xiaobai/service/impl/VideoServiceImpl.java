package com.xiaobai.service.impl;

import java.util.Date;
import java.util.List;

import com.xiaobai.mapper.*;
import com.xiaobai.pojo.*;
import com.xiaobai.pojo.vo.CommentsVO;
import com.xiaobai.pojo.vo.VideosVO;
import com.xiaobai.service.VideoService;
import com.xiaobai.utils.PagedResult;
import com.xiaobai.utils.TimeAgoUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;


import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.entity.Example.Criteria;

@Service
public class VideoServiceImpl implements VideoService {

	@Autowired
	private VideosMapper videosMapper;
	
	@Autowired
	private UsersMapper usersMapper;
	
	@Autowired
	private VideosMapperCustom videosMapperCustom;
	
	@Autowired
	private SearchRecordsMapper searchRecordsMapper;
	
	@Autowired
	private UsersLikeVideosMapper usersLikeVideosMapper;

	@Autowired
	private UsersLikeCommentMapper usersLikeCommentMapper;

	@Autowired
	private CommentsMapper commentMapper; 
	
	@Autowired
	private CommentsMapperCustom commentMapperCustom;
	
	@Autowired
	private Sid sid;
	
	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public String saveVideo(Videos video) {
		
		String id = sid.nextShort();
		video.setId(id);
		videosMapper.insertSelective(video);
		
		return id;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void updateVideo(String videoId, String coverPath) {
		
		Videos video = new Videos();
		video.setId(videoId);
		video.setCoverPath(coverPath);
		videosMapper.updateByPrimaryKeySelective(video);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public PagedResult getAllVideos(Videos video, Integer isSaveRecord,
									Integer page, Integer pageSize) {
		
		// 保存热搜词
		String desc = video.getVideoDesc();
		String userId = video.getUserId();
		if (isSaveRecord != null && isSaveRecord == 1) {
			SearchRecords record = new SearchRecords();
			String recordId = sid.nextShort();
			record.setId(recordId);
			record.setContent(desc);
			searchRecordsMapper.insert(record);
		}
		
		PageHelper.startPage(page, pageSize);
		List<VideosVO> list = videosMapperCustom.queryAllVideos(desc, userId);
		
		PageInfo<VideosVO> pageList = new PageInfo<>(list);
		
		PagedResult pagedResult = new PagedResult();
		pagedResult.setPage(page);
		pagedResult.setTotal(pageList.getPages());
		pagedResult.setRows(list);
		pagedResult.setRecords(pageList.getTotal());
		
		return pagedResult;
	}
	
	@Transactional(propagation = Propagation.SUPPORTS)
	@Override
	public PagedResult queryMyLikeVideos(String userId, Integer page, Integer pageSize) {
		PageHelper.startPage(page, pageSize);
		List<VideosVO> list = videosMapperCustom.queryMyLikeVideos(userId);
				
		PageInfo<VideosVO> pageList = new PageInfo<>(list);
		
		PagedResult pagedResult = new PagedResult();
		pagedResult.setTotal(pageList.getPages());
		pagedResult.setRows(list);
		pagedResult.setPage(page);
		pagedResult.setRecords(pageList.getTotal());
		
		return pagedResult;
	}
	
	@Transactional(propagation = Propagation.SUPPORTS)
	@Override
	public PagedResult queryMyFollowVideos(String userId, Integer page, Integer pageSize) {
		PageHelper.startPage(page, pageSize);
		List<VideosVO> list = videosMapperCustom.queryMyFollowVideos(userId);
				
		PageInfo<VideosVO> pageList = new PageInfo<>(list);
		
		PagedResult pagedResult = new PagedResult();
		pagedResult.setTotal(pageList.getPages());
		pagedResult.setRows(list);
		pagedResult.setPage(page);
		pagedResult.setRecords(pageList.getTotal());
		
		return pagedResult;
	}

	@Transactional(propagation = Propagation.SUPPORTS)
	@Override
	public List<String> getHotwords() {
		return searchRecordsMapper.getHotwords();
	}

	@Transactional(propagation = Propagation.SUPPORTS)
	@Override
	public List<String> getVideosPath() {
		List<String> videosPath = videosMapper.getVideosPath();
		for(int i=0;i<videosPath.size();i++){
			String s = videosPath.get(i);
			String path="http://192.168.43.66:8080"+s;
			videosPath.set(i,path);
		}
		return videosPath;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	/**
	 * userId:点赞者的id
	 * videoId:视频id
	 * videoCreaterId:视频发布者id
	 */
	public void userLikeVideo(String userId, String videoId, String videoCreaterId) {
		// 1. 保存用户和视频的喜欢点赞关联关系表
		String likeId = sid.nextShort();
		UsersLikeVideos ulv = new UsersLikeVideos();
		ulv.setId(likeId);
		ulv.setUserId(userId);
		ulv.setVideoId(videoId);
		usersLikeVideosMapper.insert(ulv);
		
		// 2. 视频喜欢数量累加
		videosMapperCustom.addVideoLikeCount(videoId);
		
		// 3. 用户受喜欢数量的累加
		usersMapper.addReceiveLikeCount(videoCreaterId);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void userUnLikeVideo(String userId, String videoId, String videoCreaterId) {
		// 1. 删除用户和视频的喜欢点赞关联关系表
		
		Example example = new Example(UsersLikeVideos.class);
		Criteria criteria = example.createCriteria();
		
		criteria.andEqualTo("userId", userId);
		criteria.andEqualTo("videoId", videoId);
		
		usersLikeVideosMapper.deleteByExample(example);
		
		// 2. 视频喜欢数量累减
		videosMapperCustom.reduceVideoLikeCount(videoId);
		
		// 3. 用户受喜欢数量的累减
		usersMapper.reduceReceiveLikeCount(videoCreaterId);
		
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void saveComment(Comments comment) {
		String id = sid.nextShort();
		comment.setId(id);
		comment.setCreateTime(new Date());
		commentMapper.insert(comment);
	}

	@Override
	public int commentCount(String videoId) {
		Example example = new Example(Comments.class);
		Criteria criteria = example.createCriteria();
		criteria.andEqualTo("videoId", videoId);
		int commentCount = commentMapper.selectCountByExample(example);
		return commentCount;
	}



	@Transactional(propagation = Propagation.SUPPORTS)
	@Override
	public List<CommentsVO> getAllComments(String videoId) {

		List<CommentsVO> list = commentMapperCustom.queryComments(videoId);
		
			for (CommentsVO c : list) {
				String timeAgo = TimeAgoUtils.format(c.getCreateTime());
				c.setTimeAgoStr(timeAgo);
			}
		

		
		return list;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void userLikeComment(String userId,String commentId,String commentCreaterId) {
		// 1. 保存用户和视频的喜欢点赞关联关系表
		String likeId = sid.nextShort();
		UsersLikeComment ulc = new UsersLikeComment();
		ulc.setId(likeId);
		ulc.setUserId(userId);
		ulc.setCommentId(commentId);
		usersLikeCommentMapper.insert(ulc);

		// 2. 评论喜欢数量累加
		commentMapperCustom.addCommentLikeCount(commentId);

		// 3. 用户受喜欢数量的累加
		usersMapper.addReceiveLikeCount(commentCreaterId);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void userUnLikeComment(String userId,String commentId,String commentCreaterId) {
		// 1. 删除用户和视频的喜欢点赞关联关系表

		Example example = new Example(UsersLikeComment.class);
		Criteria criteria = example.createCriteria();

		criteria.andEqualTo("userId", userId);
		criteria.andEqualTo("commentId", commentId);

		usersLikeCommentMapper.deleteByExample(example);

		// 2. 视频喜欢数量累减
		commentMapperCustom.reduceCommentLikeCount(commentId);

		// 3. 用户受喜欢数量的累减
		usersMapper.reduceReceiveLikeCount(commentCreaterId);
	}

}
