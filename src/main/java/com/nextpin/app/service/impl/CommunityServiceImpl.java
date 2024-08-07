package com.nextpin.app.service.impl;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextpin.app.dao.CommunityDao;
import com.nextpin.app.dto.CourseDto;
import com.nextpin.app.dto.CourseDetailDto;
import com.nextpin.app.service.CommunityService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommunityServiceImpl implements CommunityService {

    private Logger logger = (Logger) LoggerFactory.getLogger(CommunityServiceImpl.class);
    private final CommunityDao communityDao;

    @Autowired
    public CommunityServiceImpl(CommunityDao communityDao){
        this.communityDao = communityDao;
    }

    @Override
    public List<CourseDto> getAllCourses() {
        return communityDao.getAllCourses();
    }

    @Override
    public List<CourseDetailDto> getCourseDetailsByCourseId(int courseId) {
        return communityDao.getCourseDetailsByCourseId(courseId);
    }

    public List<CourseDetailDto> getCourseDetailByCourses(List<Integer> courseIds) {
        return communityDao.getCourseDetailByCourses(courseIds);
    }

    public List<Map<CourseDto, List<CourseDetailDto>>> getCourseListMap(){
        /*
         * -- 변경 전
         * [ {cousrId1, cousrId2, cousrId3, ...}, {detailCourse1, detailCourse2, detailCourse3, ...}]
         *
         *
         * -- 변경예정
         *[
         *   {cousrId1 : [detailCourse1, detailCourse2, detailCourse3]}
         * , {cousrId2 : [detailCourse1, detailCourse2]}
         * , {cousrId3 : [detailCourse1, detailCourse2, detailCourse3, detailCourse4]}
         * ]
         * */
        List<CourseDto> courseList = getAllCourses();
        logger.debug("courseList : " + courseList.toString());
        List<Map<CourseDto, List<CourseDetailDto>>> courseDataList = new ArrayList<>();

        List<Integer> courseIds = new ArrayList<>();
        for(CourseDto courseDto : courseList){
            courseIds.add(courseDto.getCourseId());
        }
        List<CourseDetailDto> courseDetailList = getCourseDetailByCourses(courseIds);
        logger.debug("courseDetailList : " + courseDetailList.toString());

        for(CourseDto courseDto : courseList){
            Map<CourseDto, List<CourseDetailDto>> tempMap = new HashMap<>();
            List<CourseDetailDto> tempDetailList = new ArrayList<>();
            for(CourseDetailDto courseDetailDto : courseDetailList){
                if(courseDto.getCourseId() == courseDetailDto.getCourseId()){
                    tempDetailList.add(courseDetailDto);
                }
            }
            tempDetailList = tempDetailList.stream()
                                           .sorted(Comparator.comparing(CourseDetailDto::getVisitDate))
                                           .collect(Collectors.toList());
            if(null != courseDto && tempDetailList.size() > 0){
                tempMap.put(courseDto, tempDetailList);
            }
            courseDataList.add(tempMap);
        }


        return courseDataList;
    }
}
