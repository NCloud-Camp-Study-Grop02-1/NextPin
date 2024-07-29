package com.nextpin.app.service.impl;

import ch.qos.logback.classic.Logger;
import com.nextpin.app.dto.KakaoMapDto;
import com.nextpin.app.mapper.DataMapper;
import com.nextpin.app.service.KakaoMapDataRestore;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KakaoMapDataRestoreImpl implements KakaoMapDataRestore {
    private Logger logger = (Logger) LoggerFactory.getLogger(KakaoMapDataRestoreImpl.class);
    private final DataMapper dataMapper;

    @Override
    public void restore(List<KakaoMapDto> data) {
        for(KakaoMapDto kakaoMapDto : data) {

            dataMapper.kakaoTestInsert(kakaoMapDto);

        }
    }

    @Override
    public void testRestore(KakaoMapDto kakaoMapDto) {
        logger.debug(kakaoMapDto.toString());
        dataMapper.kakaoTestInsert(kakaoMapDto);
    }
}