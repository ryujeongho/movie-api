package com.sh.app.movieapi.boxoffice.controller;

import com.sh.app.movieapi.boxoffice.entity.BoxOffice;
import com.sh.app.movieapi.boxoffice.service.BoxOfficeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class BoxOfficeController {
    @Autowired
    private BoxOfficeService boxOfficeService;

    @CrossOrigin(origins = "https://www.youtube.com")
    @GetMapping("/")
    public String showIndex(Model model) {
        List<BoxOffice> boxOfficeList = boxOfficeService.findAll();
        model.addAttribute("boxOfficeList", boxOfficeList);
        return "index";
    }
}
