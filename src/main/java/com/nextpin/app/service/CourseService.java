package com.nextpin.app.service;

import com.nextpin.app.dto.CourseDto;
import com.nextpin.app.dto.CourseDetailDto;
import com.nextpin.app.dto.Criteria;
import com.nextpin.app.dto.KakaoMapDto;
import com.nextpin.app.dto.KakaoMapReviewDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface CourseService {
    public List<KakaoMapDto> getAddressDatas();

    public List<KakaoMapDto> getUpdateForData();

    public void updateForData(List<KakaoMapDto> kakaoMapDtoList);

    public KakaoMapDto searchPinDetail(int id);

    public List<KakaoMapReviewDto> searchPinDetailReview(int id);

    public String searchPinDatas(HashMap<String, String> searchKeywords, Criteria cri);

    public int getPinDatasCnt(HashMap<String, String> searchKeywords);

    List<CourseDto> getUserCourses(String userId);

    List<Map<String, Object>> findCourseDetail(String userId);

    void updateMemo(CourseDetailDto courseDetailDto);
}
