package org.kosa.shoppingmaillmanager.host.product.category;



import java.util.List;

import org.kosa.shoppingmaillmanager.host.product.dto.CategoryTreeDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/tree")
    public List<CategoryTreeDTO> getCategoryTree() {
        return categoryService.getCategoryTree();
    }
    
    @GetMapping("/main")
    public List<CategoryTreeDTO> getMainCategories() {
        return categoryService.getMainCategories();
    }
    
    @GetMapping("/path")
    public String getCategoryPath(@RequestParam Integer categoryId) {
        List<CategoryTreeDTO> flatList = categoryService.getFlatCategoryList();
        return CategoryTreeDTO.getCategoryPath(categoryId, flatList);
    }
    
    @GetMapping("/flat")
    public List<CategoryTreeDTO> getFlatList() {
        return categoryService.getFlatCategoryList();
    }
}
