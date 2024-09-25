package io.hhplus.tdd.point.service.impl;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.model.PointHistory;
import io.hhplus.tdd.point.model.TransactionType;
import io.hhplus.tdd.point.model.UserPoint;
import io.hhplus.tdd.point.service.PointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("pointService")
public class PointServiceImpl implements PointService {

    private static final int MAX_POINT = 10_000_000;
    private static final String EXCEED = "exceed";
    private static final String SUCCESS = "success";
    private static final String INSUFFICIENT = "insufficient";
    private static final String INVALID_AMOUNT = "invalid_amount";

    @Autowired
    private UserPointTable userPointTable;

    @Autowired
    private PointHistoryTable pointHistoryTable;

    /**
     * 테스트 코드 작성을 위한 생성자 주입
     */
    public PointServiceImpl(UserPointTable userPointTable, PointHistoryTable pointHistoryTable) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
    }

    /**
     * param : id
     * 등록된 user : 현재 point 조회
     * 등록되지 않은 user : default point 0 조회
     */
    @Override
    public synchronized UserPoint selectPointById(long id) {
        return userPointTable.selectById(id);
    }

    /**
     * param : id
     * 등록된 user : history 조회
     * 등록되지 않은 user : errorException
     */
    @Override
    public synchronized List<PointHistory> selectHistoryById(long id) {
        return pointHistoryTable.selectAllByUserId(id);
    }

    /**
     * param : id, amount
     * point 충전
     */
    @Override
    public synchronized String chargePoint(long id, long amount, long now) {
        if (amount <= 0) {
            return INVALID_AMOUNT;
        }

        UserPoint userPoint = userPointTable.selectById(id);

        if (isExceedingMaxPoint(userPoint.point(), amount)) {
            return EXCEED;
        }

        updatePoint(id, userPoint.point() + amount, TransactionType.CHARGE);
        return SUCCESS;
    }

    /**
     * param : id, amount
     * point 사용
     */
    @Override
    public synchronized String usePoint(long id, long amount, long now) {
        if (amount <= 0) {
            return INVALID_AMOUNT;
        }

        UserPoint userPoint = userPointTable.selectById(id);

        if (hasInsufficientPoints(userPoint, amount)) {
            return INSUFFICIENT;
        }

        updatePoint(id, userPoint.point() - amount, TransactionType.USE);
        return SUCCESS;
    }

    /**
     * param : userPoint, amount
     * 충전 후 포인트가 최대 포인트를 초과하는지 확인
     */
    private boolean isExceedingMaxPoint(long userPoint, long amount) {
        return userPoint + amount > MAX_POINT;
    }

    /**
     * param : userPoint, amount
     * 사용 전 포인트가 현재 보유 포인트를 초과하는지 확인
     */
    private boolean hasInsufficientPoints(UserPoint userPoint, long amount) {
        return userPoint.point() < amount;
    }

    /**
     * param : id, newPointAmount
     * 포인트를 충전(chargePoint) / 사용(usePoint) 한 경우 history를 남기고, 최종 Point로 setting
     */
    private void updatePoint(long id, long amount, TransactionType transactionType) {
        pointHistoryTable.insert(id, amount, transactionType, System.currentTimeMillis());
        userPointTable.insertOrUpdate(id, amount);
    }
}
