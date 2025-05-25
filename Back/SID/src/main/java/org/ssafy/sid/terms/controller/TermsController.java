package org.ssafy.sid.terms.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.ssafy.sid.terms.model.TermsList;
import org.ssafy.sid.terms.model.TermsListRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/terms")
public class TermsController {

	private final TermsListRepository termsListRepository;

	public TermsController(TermsListRepository termsListRepository) {
		this.termsListRepository = termsListRepository;
	}

	@GetMapping
	public ResponseEntity<?> getTerms() {
		Map<String, Object> resultMap = new HashMap<>();
		Map<String, Object> errorResultMap = new HashMap<>();
		List<TermsList> termsLists = termsListRepository.findAll();
		if (termsLists.isEmpty()) {
			errorResultMap.put("error", "No terms found");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResultMap);
		}

		for (TermsList termsList : termsLists) {
			resultMap.put(termsList.getTitle(), termsList.getDescription());
			resultMap.put("필수 동의 여부", termsList.getIsRequired());
		}
		return ResponseEntity.status(HttpStatus.OK).body(resultMap);
	}
}
