package org.ssafy.sid.categories.service;

import org.ssafy.sid.categories.dto.*;
import org.ssafy.sid.categories.model.Categories;
import org.ssafy.sid.profiles.model.Profiles;

import java.util.List;

public interface CategoriesService {
	void createCategory(CategorySaveDTO categorySaveDTO);
	void updateCategory(CategoryDTO categoryDTO, Categories category);
	void deleteCategory(Categories category);
	List<Categories> getCategories();
	void addInterestCategory(InterestCategorySaveDTO interestCategorySaveDTO);
	List<InterestCategoryGetDTO> getInterestCategories(Profiles profile);
}
