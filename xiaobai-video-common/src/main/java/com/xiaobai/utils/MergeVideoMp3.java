package com.xiaobai.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MergeVideoMp3 {

	private String ffmpegEXE;
	
	public MergeVideoMp3(String ffmpegEXE) {
		super();
		this.ffmpegEXE = ffmpegEXE;
	}

	public String removeTrack(String videoInputPath,String videoOutputPath) throws IOException {
		//ffmpeg.exe -i 12.28.mp4 -vcodec copy -an 12.28.noaudio.mp4

		List<String> command = new ArrayList<>();
		command.add(ffmpegEXE);

		command.add("-i");
		command.add(videoInputPath);


		command.add("-vcodec");
		command.add("copy");
		command.add("-an");

		command.add(videoOutputPath);

//		for (String c : command) {
//			System.out.print(c + " ");
//		}

		//java提供的dos命令的API
		ProcessBuilder builder = new ProcessBuilder(command);
		Process process = builder.start();

		//当执行命令时，会产生一些临时文件碎片占用cpu和内存空间。
		//命令处理时会产生一些流,流会占用内存，如果流过多就会卡住我们的线程。
		//这里做一个流的处理
		InputStream errorStream = process.getErrorStream();
		InputStreamReader inputStreamReader = new InputStreamReader(errorStream);
		BufferedReader br = new BufferedReader(inputStreamReader);

		String line = "";
		while ( (line = br.readLine()) != null ) {
		}

		if (br != null) {
			br.close();
		}
		if (inputStreamReader != null) {
			inputStreamReader.close();
		}
		if (errorStream != null) {
			errorStream.close();
		}

		return videoOutputPath;
	}

	public void convertor(String videoInputPath, String mp3InputPath,
			double seconds, String videoOutputPath) throws Exception {
//		ffmpeg.exe -i 苏州大裤衩.mp4 -i bgm.mp3 -t 7 -y 新的视频.mp4

		List<String> command = new ArrayList<>();
		command.add(ffmpegEXE);
		
		command.add("-i");
		command.add(videoInputPath);
		
		command.add("-i");
		command.add(mp3InputPath);
		
		command.add("-t");
		command.add(String.valueOf(seconds));
		
		command.add("-y");
		command.add(videoOutputPath);
		
//		for (String c : command) {
//			System.out.print(c + " ");
//		}

		//java提供的dos命令的API
		ProcessBuilder builder = new ProcessBuilder(command);
		Process process = builder.start();

		//当执行命令时，会产生一些临时文件碎片占用cpu和内存空间。
		//命令处理时会产生一些流,流会占用内存，如果流过多就会卡住我们的线程。
		//这里做一个流的处理
		InputStream errorStream = process.getErrorStream();
		InputStreamReader inputStreamReader = new InputStreamReader(errorStream);
		BufferedReader br = new BufferedReader(inputStreamReader);
		
		String line = "";
		while ( (line = br.readLine()) != null ) {
		}
		
		if (br != null) {
			br.close();
		}
		if (inputStreamReader != null) {
			inputStreamReader.close();
		}
		if (errorStream != null) {
			errorStream.close();
		}
		
	}

	public static void main(String[] args) {
		MergeVideoMp3 ffmpeg = new MergeVideoMp3("D:\\ffmpeg\\bin\\ffmpeg.exe");
		try {
			ffmpeg.removeTrack("D:\\UpupooResource\\1800014158\\HEXGRID.mp4","D:\\UpupooResource\\1800014158\\HEXGRID2.mp4");
			ffmpeg.convertor("D:\\UpupooResource\\1800014158\\HEXGRID2.mp4", "C:\\Users\\Lenovo\\Music\\CG - 文爱.mp3", 7, "D:\\这是通过java生产的视频2.mp4");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
