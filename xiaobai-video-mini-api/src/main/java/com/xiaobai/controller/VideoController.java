package com.xiaobai.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.xiaobai.enums.VideoStatusEnum;
import com.xiaobai.pojo.Bgm;
import com.xiaobai.pojo.Comments;
import com.xiaobai.pojo.Videos;
import com.xiaobai.pojo.vo.CommentsVO;
import com.xiaobai.service.BgmService;
import com.xiaobai.service.VideoService;
import com.xiaobai.utils.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;


@RestController
@Api(value="视频相关业务的接口", tags= {"视频相关业务的controller"})
@RequestMapping("/video")
public class VideoController extends BasicController {
	
	@Autowired
	private BgmService bgmService;
	
	@Autowired
	private VideoService videoService;
	
	@ApiOperation(value="上传视频", notes="上传视频的接口")
	@ApiImplicitParams({
		@ApiImplicitParam(name="userId", value="用户id", required=true, 
				dataType="String", paramType="form"),
		@ApiImplicitParam(name="bgmId", value="背景音乐id",
				dataType="String", paramType="form"),
		@ApiImplicitParam(name="videoSeconds", value="背景音乐播放长度", required=true, 
				dataType="String", paramType="form"),
		@ApiImplicitParam(name="videoWidth", value="视频宽度", required=true, 
				dataType="String", paramType="form"),
		@ApiImplicitParam(name="videoHeight", value="视频高度", required=true, 
				dataType="String", paramType="form"),
		@ApiImplicitParam(name="desc", value="视频描述",
				dataType="String", paramType="form")
	})
	@PostMapping(value="/upload", headers="content-type=multipart/form-data")
	public IMoocJSONResult upload(String userId,
								  String bgmId, double videoSeconds,
								  int videoWidth, int videoHeight, String desc,
								  @ApiParam(value="短视频", required=true) MultipartFile file) throws Exception {
		
		if (StringUtils.isBlank(userId)) {
			return IMoocJSONResult.errorMsg("用户id不能为空...");
		}
		
		// 文件保存的命名空间
//		String fileSpace = "C:/imooc_videos_dev";
		// 保存到数据库中的相对路径
		String uploadPathDB = "/" + userId + "/video";
		String coverPathDB = "/" + userId + "/video";
		
		FileOutputStream fileOutputStream = null;
		InputStream inputStream = null;
		// 文件上传的最终保存路径
		String finalVideoPath = "";
		try {
			if (file != null) {
				
				String fileName = file.getOriginalFilename();
				// abc.mp4
				String arrayFilenameItem[] =  fileName.split("\\.");
				String fileNamePrefix = "";
				for (int i = 0 ; i < arrayFilenameItem.length-1 ; i ++) {
					fileNamePrefix += arrayFilenameItem[i];
				}
				// fix bug: 解决小程序端OK，PC端不OK的bug，原因：PC端和小程序端对临时视频的命名不同
//				String fileNamePrefix = fileName.split("\\.")[0];
				
				if (StringUtils.isNotBlank(fileName)) {
					
					finalVideoPath = FILE_SPACE + uploadPathDB + "/" + fileName;
					// 设置数据库保存的路径
					uploadPathDB += ("/" + fileName);
					coverPathDB = coverPathDB + "/" + fileNamePrefix + ".jpg";
					
					File outFile = new File(finalVideoPath);
					if (outFile.getParentFile() != null || !outFile.getParentFile().isDirectory()) {
						// 创建父文件夹
						outFile.getParentFile().mkdirs();
					}
					
					fileOutputStream = new FileOutputStream(outFile);
					inputStream = file.getInputStream();
					IOUtils.copy(inputStream, fileOutputStream);
				}
				
			} else {
				return IMoocJSONResult.errorMsg("上传出错...");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return IMoocJSONResult.errorMsg("上传出错...");
		} finally {
			if (fileOutputStream != null) {
				fileOutputStream.flush();
				fileOutputStream.close();
			}
		}
		
		// 判断bgmId是否为空，如果不为空，
		// 那就查询bgm的信息，并且合并视频，生产新的视频
		if (StringUtils.isNotBlank(bgmId)) {
			Bgm bgm = bgmService.queryBgmById(bgmId);
			String mp3InputPath = FILE_SPACE + bgm.getPath();
			
			MergeVideoMp3 tool = new MergeVideoMp3(FFMPEG_EXE);
			//设置需要修改的视频路径
			String videoInputPath = finalVideoPath;
			String arrayPathnameItem[] =  videoInputPath.split("\\.");
			String removeTrackOutPutPath=arrayPathnameItem[0]+1+".mp4";
			//去除音轨
			String removeTrack = tool.removeTrack(videoInputPath, removeTrackOutPutPath);
			String videoOutputName = UUID.randomUUID().toString() + ".mp4";
			uploadPathDB = "/" + userId + "/video" + "/" + videoOutputName;
			finalVideoPath = FILE_SPACE + uploadPathDB;
			//添加音频
			tool.convertor(removeTrack, mp3InputPath, videoSeconds, finalVideoPath);
			DeleteInvalidFile deleteInvalidFile=new DeleteInvalidFile();
			deleteInvalidFile.deleteFolder(videoInputPath);
			deleteInvalidFile.deleteFolder(removeTrack);
		}
		System.out.println("uploadPathDB=" + uploadPathDB);
		System.out.println("finalVideoPath=" + finalVideoPath);
		
		// 对视频进行截图
		FetchVideoCover videoInfo = new FetchVideoCover(FFMPEG_EXE);
		videoInfo.getCover(finalVideoPath, FILE_SPACE + coverPathDB);
		
		// 保存视频信息到数据库
		Videos video = new Videos();
		video.setAudioId(bgmId);
		video.setUserId(userId);
		video.setVideoSeconds((float)videoSeconds);
		video.setVideoHeight(videoHeight);
		video.setVideoWidth(videoWidth);
		video.setVideoDesc(desc);
		video.setVideoPath(uploadPathDB);
		video.setCoverPath(coverPathDB);
		video.setStatus(VideoStatusEnum.SUCCESS.value);
		video.setCreateTime(new Date());
		
		String videoId = videoService.saveVideo(video);

		return IMoocJSONResult.ok(videoId);
	}
	
	@ApiOperation(value="上传封面", notes="上传封面的接口")
	@ApiImplicitParams({
		@ApiImplicitParam(name="userId", value="用户id", required=true, 
				dataType="String", paramType="form"),
		@ApiImplicitParam(name="videoId", value="视频主键id", required=true, 
				dataType="String", paramType="form")
	})
	@PostMapping(value="/uploadCover", headers="content-type=multipart/form-data")
	public IMoocJSONResult uploadCover(String userId,
				String videoId,
				@ApiParam(value="视频封面", required=true)
				MultipartFile file) throws Exception {
		
		if (StringUtils.isBlank(videoId) || StringUtils.isBlank(userId)) {
			return IMoocJSONResult.errorMsg("视频主键id和用户id不能为空...");
		}
		
		// 文件保存的命名空间
//		String fileSpace = "C:/imooc_videos_dev";
		// 保存到数据库中的相对路径
		String uploadPathDB = "/" + userId + "/video";
		
		FileOutputStream fileOutputStream = null;
		InputStream inputStream = null;
		// 文件上传的最终保存路径
		String finalCoverPath = "";
		try {
			if (file != null) {
				
				String fileName = file.getOriginalFilename();
				if (StringUtils.isNotBlank(fileName)) {
					
					finalCoverPath = FILE_SPACE + uploadPathDB + "/" + fileName;
					// 设置数据库保存的路径
					uploadPathDB += ("/" + fileName);
					
					File outFile = new File(finalCoverPath);
					if (outFile.getParentFile() != null || !outFile.getParentFile().isDirectory()) {
						// 创建父文件夹
						outFile.getParentFile().mkdirs();
					}
					
					fileOutputStream = new FileOutputStream(outFile);
					inputStream = file.getInputStream();
					IOUtils.copy(inputStream, fileOutputStream);
				}
				
			} else {
				return IMoocJSONResult.errorMsg("上传出错...");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return IMoocJSONResult.errorMsg("上传出错...");
		} finally {
			if (fileOutputStream != null) {
				fileOutputStream.flush();
				fileOutputStream.close();
			}
		}
		
		videoService.updateVideo(videoId, uploadPathDB);
		
		return IMoocJSONResult.ok();
	}
	
	/**
	 * 
	 * @Description: 分页和搜索查询视频列表
	 * isSaveRecord：1 - 需要保存
	 * 				 0 - 不需要保存 ，或者为空的时候
	 */
	@PostMapping(value="/showAll")
	public IMoocJSONResult showAll(@RequestBody Videos video, Integer isSaveRecord,
			Integer page, Integer pageSize) throws Exception {
		
		if (page == null) {
			page = 1;
		}
		
		if (pageSize == null) {
			pageSize = PAGE_SIZE;
		}
		
		PagedResult result = videoService.getAllVideos(video, isSaveRecord, page, pageSize);
		return IMoocJSONResult.ok(result);
	}
	
	/**
	 * @Description: 我关注的人发的视频
	 */
	@PostMapping("/showMyFollow")
	public IMoocJSONResult showMyFollow(String userId, Integer page) throws Exception {
		
		if (StringUtils.isBlank(userId)) {
			return IMoocJSONResult.ok();
		}
		
		if (page == null) {
			page = 1;
		}

		int pageSize = 6;
		
		PagedResult videosList = videoService.queryMyFollowVideos(userId, page, pageSize);
		
		return IMoocJSONResult.ok(videosList);
	}
	
	/**
	 * @Description: 我收藏(点赞)过的视频列表
	 */
	@PostMapping("/showMyLike")
	public IMoocJSONResult showMyLike(String userId, Integer page, Integer pageSize) throws Exception {
		
		if (StringUtils.isBlank(userId)) {
			return IMoocJSONResult.ok();
		}
		
		if (page == null) {
			page = 1;
		}

		if (pageSize == null) {
			pageSize = 6;
		}
		
		PagedResult videosList = videoService.queryMyLikeVideos(userId, page, pageSize);
		
		return IMoocJSONResult.ok(videosList);
	}

	//热搜词前16条
	@PostMapping(value="/hot")
	public IMoocJSONResult hot() throws Exception {
		return IMoocJSONResult.ok(videoService.getHotwords());
	}

	//视频路径
	@PostMapping(value="/videosPath")
	public IMoocJSONResult videosPath() throws Exception {
		return IMoocJSONResult.ok(videoService.getVideosPath());
	}

	//点赞
	@PostMapping(value="/userLike")
	public IMoocJSONResult userLike(String userId, String videoId, String videoCreaterId) 
			throws Exception {
		videoService.userLikeVideo(userId, videoId, videoCreaterId);
		return IMoocJSONResult.ok();
	}

	//取消点赞
	@PostMapping(value="/userUnLike")
	public IMoocJSONResult userUnLike(String userId, String videoId, String videoCreaterId) throws Exception {
		videoService.userUnLikeVideo(userId, videoId, videoCreaterId);
		return IMoocJSONResult.ok();
	}

	//发表评论
	@PostMapping("/saveComment")
	public IMoocJSONResult saveComment(@RequestBody Comments comment,
			String fatherCommentId, String toUserId) throws Exception {

        if ( comment==null||comment.equals("")) {
            return IMoocJSONResult.ok();
        }
		comment.setFatherCommentId(fatherCommentId);
		comment.setToUserId(toUserId);
		
		videoService.saveComment(comment);
		return IMoocJSONResult.ok();
	}

	//评论统计
	@ApiOperation(value="查询评论总量", notes="查询评论总量的接口")
	@ApiImplicitParam(name="videoId", value="视频id", required=true,
			dataType="String", paramType="query")
	@PostMapping("/commentCount")
	public IMoocJSONResult commentCount(String videoId) throws Exception {

		int commentCount = videoService.commentCount(videoId);

		return IMoocJSONResult.ok(commentCount);
	}
	//留言分页
	@PostMapping("/getVideoComments")
	public IMoocJSONResult getVideoComments(String videoId) throws Exception {
		
		if (StringUtils.isBlank(videoId)) {
			return IMoocJSONResult.ok();
		}

		List<CommentsVO> list = videoService.getAllComments(videoId);

		return IMoocJSONResult.ok(list);
	}

	//评论点赞
	@PostMapping(value="/userLikeComment")
	public IMoocJSONResult userLikeComment(String userId,String commentId,String commentCreaterId) {
		videoService.userLikeComment(userId, commentId, commentCreaterId);
		return IMoocJSONResult.ok();
	}

	//评论取消点赞
	@PostMapping(value="/userUnLikeComment")
	public IMoocJSONResult userUnLikeComment(String userId,String commentId,String commentCreaterId) {
		videoService.userUnLikeComment(userId, commentId, commentCreaterId);
		return IMoocJSONResult.ok();
	}
	
}
