package org.ssafy.sid.images.service;

import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.IOException;

public interface ImageSaveService {
	String saveImage(MultipartFile image, String type) throws IOException;
	String checkImage(String image) throws IOException;
}
