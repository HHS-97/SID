package org.ssafy.sid.images.service;

import jakarta.mail.Multipart;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageSaveServiceImpl implements ImageSaveService {

	@Value("${file.upload-dir}")
	private String uploadDir;

	@Override
	@Transactional
	public String saveImage(MultipartFile image, String type) throws IOException {

		// image가 null인지 체크
		if (image == null || image.isEmpty()) {
			return "";
		}

		// 이미지 파일 저장을 위한 경로 설정
		// type은 프로필인지 아니면 게시글인지 같은 형식
		String uploadsDir = uploadDir + type + "/";
		// 파일 이름 생성
		String fileName = UUID.randomUUID().toString().replace("-", "") + "_" + image.getOriginalFilename();
		// 실제 파일이 저장될 경로
		String filePath = uploadsDir + fileName;

		Path path = Paths.get(filePath); // Path 객체 생성
		Files.createDirectories(path.getParent()); // 디렉토리 생성
		Files.write(path, image.getBytes()); // 디렉토리에 파일 저장

		return filePath;
	}

	@Override
	@Transactional
	public String checkImage(String image) throws IOException {
		if (image != null && !image.isEmpty()) {
			// 만약 profileImageUrl이 상대 경로라면, 현재 작업 디렉토리(user.dir) 기준으로 File 객체 생성
			File imageFile = new File(System.getProperty("user.dir"), image);
			if (!imageFile.exists()) {
				return "";
			}
		} else {
			return "";
		}

		return image;
	}

}
