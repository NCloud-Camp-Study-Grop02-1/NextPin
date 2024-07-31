package com.nextpin.app.controller;

import ch.qos.logback.classic.Logger;
import com.nextpin.app.dto.CourseDto;
import com.nextpin.app.dto.Criteria;
import com.nextpin.app.dto.KakaoMapDto;
import com.nextpin.app.service.CourseService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.HashMap;

@RestController
public class CourseController {

    private Logger logger = (Logger) LoggerFactory.getLogger(CourseController.class);
    private CourseService courseService;

    @Autowired
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/courseHomeReview2")
    public ModelAndView courseHomeReview2(@RequestParam(value = "id", required = false, defaultValue = "1") int id) {
        KakaoMapDto rtnKaMapDto = courseService.searchPinDetail(id);

        ModelAndView mav = new ModelAndView();
        mav.setViewName("course/courseHomeReview2");
        mav.addObject("rtnKaMapDto", rtnKaMapDto);

        logger.debug("courseHomeReview2페이지 이동");
        return mav;
    }

    @PostMapping("/courseHomeReview2")
    @ResponseBody
    public ResponseEntity<?> createCourse(@RequestBody CourseDto.CourseDTO courseDTO) {
        try {
            // courseDTO.setUserId(getUserIdFromSession());
            courseDTO.setRegDate(LocalDateTime.now());
            Long courseId = courseService.createCourse(courseDTO);
            return ResponseEntity.ok().body(courseId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating course: " + e.getMessage());
        }
    }

    @GetMapping("/mainCourse")
    public ModelAndView mainCourse() {
        ModelAndView mav = new ModelAndView();
        mav.setViewName("course/mainCourse");

        logger.debug("mainCourse페이지 이동");
        return mav;
    }

    @PostMapping("/searchPlaces")
    @ResponseBody
    public String searchPlaces(@RequestBody HashMap<String, String> searchKeywords, Criteria cri) {
        return courseService.searchPinDatas(searchKeywords, cri);
    }
}
