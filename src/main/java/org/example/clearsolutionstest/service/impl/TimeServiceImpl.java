package org.example.clearsolutionstest.service.impl;

import org.example.clearsolutionstest.service.TimeService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TimeServiceImpl implements TimeService {

    @Override
    public LocalDateTime utcNow() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

}
