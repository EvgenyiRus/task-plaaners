package com.tasklist.development.service;

import com.tasklist.development.entity.Stat;
import com.tasklist.development.repository.StatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
/*  Все методы должны выполниться без ошибок, чтобы транзакция завершилась
    Если возникнет исключение, то все выполненные операции откатятся (Rollback) */
@Transactional
public class StatService {
    StatRepository statRepository;

    @Autowired
    public StatService(StatRepository statRepository) {
        this.statRepository = statRepository;
    }

    public Stat getByUserEmail(String email) {
        return statRepository.findByUserEmailOrderByUserEmailAsc(email);
    }
}
