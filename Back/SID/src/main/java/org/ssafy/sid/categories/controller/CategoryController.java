package org.ssafy.sid.categories.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ssafy.sid.categories.dto.CategoryDTO;
import org.ssafy.sid.categories.dto.CategoryDeleteDTO;
import org.ssafy.sid.categories.dto.CategorySaveDTO;
import org.ssafy.sid.categories.model.Categories;
import org.ssafy.sid.categories.model.CategoriesRepository;
import org.ssafy.sid.categories.service.CategoriesServiceImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/category")
public class CategoryController {

	private final CategoriesServiceImpl categoriesServiceImpl;
	private final CategoriesRepository categoriesRepository;

	@PostMapping
	public ResponseEntity<?> addCategory(@RequestBody CategorySaveDTO categorySaveDTO, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> errorResultMap = new HashMap<String, Object>();
		if (!categorySaveDTO.getIsAdmin()) {
			errorResultMap.put("error", "관리자 계정이 아닙니다.");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResultMap);
		}

		if (categoriesRepository.existsByTag(categorySaveDTO.getTag())) {
			errorResultMap.put("error", "이미 카테고리가 존재합니다.");
			return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResultMap);
		}

		categoriesServiceImpl.createCategory(categorySaveDTO);
		resultMap.put("message", "create");

		return ResponseEntity.status(HttpStatus.CREATED).body(resultMap);
	}

	@PatchMapping
	public ResponseEntity<?> updateCategory(@RequestBody CategoryDTO categoryDTO, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> errorResultMap = new HashMap<String, Object>();

		if (!categoryDTO.getIsAdmin()) {
			errorResultMap.put("error", "관리자 계정이 아닙니다.");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResultMap);
		}

		Optional<Categories> categories = categoriesRepository.findById(categoryDTO.getCategoryId());
		if (categories.isPresent()) {
			categoriesServiceImpl.updateCategory(categoryDTO, categories.get());
		} else {
			errorResultMap.put("error", "카테고리가 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}
		resultMap.put("message", "update");
		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}

	@DeleteMapping
	public ResponseEntity<?> deleteCategory(@RequestBody CategoryDeleteDTO categoryDeleteDTO, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Map<String, Object> errorResultMap = new HashMap<String, Object>();

		if (!categoryDeleteDTO.getIsAdmin()) {
			errorResultMap.put("error", "관리자 계정이 아닙니다.");
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResultMap);
		}

		Optional<Categories> categories = categoriesRepository.findById(categoryDeleteDTO.getCategoryId());
		if (categories.isPresent()) {
			categoriesServiceImpl.deleteCategory(categories.get());
		} else {
			errorResultMap.put("error", "카테고리가 존재하지 않습니다.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}
		resultMap.put("message", "delete");
		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}

	@GetMapping
	public ResponseEntity<?> getAllCategories(HttpServletRequest request, HttpServletResponse response) {
		return ResponseEntity.status(HttpStatus.OK).body(categoriesRepository.findAll());
	}
}
