package io.hhplus.tdd.point.service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.point.model.PointHistory;
import io.hhplus.tdd.point.model.UserPoint;

import java.util.List;

public interface PointService {

    /**
     * param : id

     * 등록된 user : 현재 point 조회
     * 등록되지 않은 user : default point 0 조회
     */
    UserPoint selectPointById(long id);

    /**
     * param : id

     * 등록된 user : history 조회
     * 등록되지 않은 user : errorException
     */
    List<PointHistory> selectHistoryById(long id);

    /**
     * param : id, amount

     * point 충전
     */
    String chargePoint(long id, long amount, long now);

    /**
     * param : id, amount

     * point 사용
     */
    String usePoint(long id, long amount, long now);
}