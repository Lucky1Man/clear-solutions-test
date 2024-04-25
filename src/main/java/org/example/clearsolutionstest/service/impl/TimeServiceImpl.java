package org.example.clearsolutionstest.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.example.clearsolutionstest.service.TimeService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@Slf4j
public class TimeServiceImpl implements TimeService {

    @Override
    public LocalDateTime utcNow() {
        log.debug("utcNow");
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        log.debug("end utcNow {}", now);
        return now;
    }

}
