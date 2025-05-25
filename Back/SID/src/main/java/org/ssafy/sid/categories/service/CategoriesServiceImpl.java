package org.ssafy.sid.categories.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.ssafy.sid.categories.dto.*;
import org.ssafy.sid.categories.model.Categories;
import org.ssafy.sid.categories.model.CategoriesRepository;
import org.ssafy.sid.categories.model.InterestCategories;
import org.ssafy.sid.categories.model.InterestCategoriesRepository;
import org.ssafy.sid.profiles.model.Profiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoriesServiceImpl implements CategoriesService {

	private final CategoriesRepository categoriesRepository;
	private final InterestCategoriesRepository interestCategoriesRepository;

	@Override
	@Transactional
	public void createCategory(CategorySaveDTO categorySaveDTO) {
		categoriesRepository.save(categorySaveDTO.toEntity());
	}

	@Override
	@Transactional
	public void updateCategory(CategoryDTO categoryDTO, Categories category) {
		category.update(categoryDTO);
	}

	@Override
	@Transactional
	public void deleteCategory(Categories category) {
		categoriesRepository.delete(category);
	}

	@Override
	@Transactional
	public List<Categories> getCategories() {
		return categoriesRepository.findAll();
	}

	@Override
	@Transactional
	public void addInterestCategory(InterestCategorySaveDTO interestCategorySaveDTO) {
		interestCategoriesRepository.save(interestCategorySaveDTO.toEntity());
	}

	@Override
	@Transactional
	public List<InterestCategoryGetDTO> getInterestCategories(Profiles profile) {
		List<InterestCategories> interestCategories = interestCategoriesRepository.findByProfile(profile);
		List<InterestCategoryGetDTO> interestCategoryGetDTOs = new ArrayList<>();

		for (InterestCategories interestCategory : interestCategories) {
			interestCategoryGetDTOs.add(InterestCategoryGetDTO.builder()
					.interestCategoryId(interestCategory.getCategory().getId())
					.interestCategoryName(interestCategory.getCategory().getTag())
					.build());
		}
		return interestCategoryGetDTOs;
	}
}
