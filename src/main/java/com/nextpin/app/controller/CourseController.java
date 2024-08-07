package com.nextpin.app.controller;

import ch.qos.logback.classic.Logger;
import com.nextpin.app.dto.*;
import com.nextpin.app.service.CourseHomeReview2Service;
import com.nextpin.app.service.CourseService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.springframework.web.bind.annotation.SessionAttributes;

@RestController
@SessionAttributes("user")
public class CourseController {

    private Logger logger = (Logger) LoggerFactory.getLogger(CourseController.class);
    private final CourseService courseService;
    private final CourseHomeReview2Service courseHomeReview2Service;

    @Autowired
    public CourseController(CourseService courseService, CourseHomeReview2Service courseHomeReview2Service) {
        this.courseService = courseService;
        this.courseHomeReview2Service = courseHomeReview2Service;
    }

    @GetMapping("/courseHomeReview2")
    public ModelAndView courseHomeReview2(@RequestParam(value = "id", required = false, defaultValue = "1") int id) {
        KakaoMapDto rtnKaMapDto = courseService.searchPinDetail(id);

        List<KakaoMapReviewDto> rtnKaMapReviewList = courseService.searchPinDetailReview(id);
        int rtnKaMapReviewListSize = rtnKaMapReviewList.size();

        logger.debug("리뷰 리스트 : " + rtnKaMapReviewList);
        ModelAndView mav = new ModelAndView();
        mav.setViewName("course/courseHomeReview2");
        mav.addObject("rtnKaMapDto", rtnKaMapDto);
        mav.addObject("rtnKaMapReviewList", rtnKaMapReviewList);
        mav.addObject("rtnKaMapReviewListCnt", rtnKaMapReviewListSize);

        logger.debug("courseHomeReview2페이지 이동");
        return mav;
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

    @PostMapping("/createOrUpdateCourse")
    public String createOrUpdateCourse(@RequestBody HashMap<String, Object> requestData) {
        try {
            CourseDto course = new CourseDto();
            course.setUserId((String) requestData.get("userId"));
            course.setNickname((String) requestData.get("nickname"));
            course.setCourseName((String) requestData.get("courseName"));
            course.setColor((String) requestData.get("color"));
            course.setMyPinBoolean(true);
            course.setLikeBoolean(false);

            HashMap<String, Object> courseDetailMap = (HashMap<String, Object>) requestData.get("courseDetail");
            CourseDetailDto courseDetail = new CourseDetailDto();
            courseDetail.setUserId((String) requestData.get("userId"));
            courseDetail.setLocation((String) courseDetailMap.get("location"));
            courseDetail.setX((Double) courseDetailMap.get("x"));
            courseDetail.setY((Double) courseDetailMap.get("y"));

            String visitDateString = (String) courseDetailMap.get("visitDate");
            if (visitDateString != null && !visitDateString.isEmpty()) {
                try {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
                    Date visitDate = formatter.parse(visitDateString);
                    courseDetail.setVisitDate(visitDate);
                } catch (ParseException e) {
                    throw new IllegalArgumentException("잘못된 날짜 형식: " + visitDateString, e);
                }
            } else {
                throw new IllegalArgumentException("visitDate 값이 null이거나 비어 있습니다.");
            }
            courseDetail.setMemo((String) courseDetailMap.get("memo"));

            Integer existingCourseId = courseHomeReview2Service.findCourseIdByUserIdAndCourseName(course.getUserId(), course.getCourseName());
            if (existingCourseId == null) {
                // 코스가 없으면 새로 삽입
                courseHomeReview2Service.insertCourse(course);
                existingCourseId = courseHomeReview2Service.findCourseIdByUserIdAndCourseName(course.getUserId(), course.getCourseName());
                if (existingCourseId == null) {
                    throw new IllegalStateException("새로 삽입된 코스의 ID를 찾을 수 없습니다.");
                }
            } else {
                // 코스가 있으면 색상과 수정일을 업데이트
                course.setCourseId(existingCourseId);
                courseHomeReview2Service.updateCourseColorAndModifyDate(existingCourseId, course.getColor());
            }

            if (courseHomeReview2Service.isLocationExist(existingCourseId, courseDetail.getLocation())) {
                return "{\"status\":\"error\", \"message\":\"중복된 코스가 있습니다.\"}";
            }

            courseDetail.setCourseId(existingCourseId);
            courseHomeReview2Service.insertCourseDetail(courseDetail);

            return "{\"status\":\"success\", \"existing\": true}";
        } catch (Exception e) {
            logger.error("코스 생성 중 오류 발생", e);
            return "{\"status\":\"error\", \"message\":\"코스 생성에 실패하였습니다.\"}";
        }
    }

    @PostMapping("/insertCourseDetail")
    public HashMap<String, String> insertCourseDetail(@RequestBody CourseDetailDto courseDetail) {
        courseHomeReview2Service.insertCourseDetail(courseDetail);
        HashMap<String, String> response = new HashMap<>();
        response.put("status", "success");
        return response;
    }

    @GetMapping("/getCourseDetails")
    public Map<String, Object> getCourseDetails(@RequestParam String courseName, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        String userId = (String) session.getAttribute("userId");

        if (userId == null) {
            response.put("status", "error");
            response.put("message", "로그인이 필요합니다.22222222");
            return response;
        }

        try {
            Integer courseId = courseHomeReview2Service.findCourseIdByUserIdAndCourseName(userId, courseName);
            if (courseId == null) {
                response.put("status", "error");
                response.put("message", "코스를 찾을 수 없습니다.");
                return response;
            }

            List<CourseDetailDto> courseDetails = courseHomeReview2Service.getCourseDetails(courseId);
            response.put("status", "success");
            response.put("courseList", courseDetails);
            logger.debug("Course details fetched: " + courseDetails); // 디버깅 로그 추가
        } catch (Exception e) {
            logger.error("코스 세부 정보 가져오기 중 오류 발생", e);
            response.put("status", "error");
            response.put("message", "코스 세부 정보를 가져오는 데 실패하였습니다.");
        }
        return response;
    }

    @GetMapping("/getCoursesByUserId")
    @ResponseBody
    public List<String> getCoursesByUserId(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return Collections.emptyList();
        }
        return courseHomeReview2Service.getCoursesByUserId(userId);
    }

}
