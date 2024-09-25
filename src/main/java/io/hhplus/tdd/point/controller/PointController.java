package io.hhplus.tdd.point.controller;

import io.hhplus.tdd.ErrorResponse;
import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.HandlePointException;
import io.hhplus.tdd.point.model.PointHistory;
import io.hhplus.tdd.point.model.TransactionType;
import io.hhplus.tdd.point.model.UserPoint;
import io.hhplus.tdd.point.service.PointService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/point")
public class PointController {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);

    @Resource(name = "pointService")
    private PointService pointService;

    /**
     * TODO - 특정 유저의 포인트를 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}")
    public UserPoint point(
            @PathVariable long id
    ) {
        UserPoint userPoint = pointService.selectPointById(id);
        return new UserPoint(id, userPoint.point(), System.currentTimeMillis());
    }

    /**
     * TODO - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}/histories")
    public List<PointHistory> history(
            @PathVariable long id
    ) {
        return pointService.selectHistoryById(id);
    }

    /**
     * TODO - 특정 유저의 포인트를 충전하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/charge")
    public UserPoint charge(
            @PathVariable long id,
            @RequestBody long amount
    ) {
        String result = pointService.chargePoint(id, amount, System.currentTimeMillis());
        if ("exceed".equals(result)) {
            throw new HandlePointException("최대 포인트를 초과했습니다.");
        }

        UserPoint updatedUserPoint = pointService.selectPointById(id);
        return new UserPoint(id, updatedUserPoint.point(), System.currentTimeMillis());
    }

    /**
     * TODO - 특정 유저의 포인트를 사용하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/use")
    public UserPoint use(
            @PathVariable long id,
            @RequestBody long amount
    ) {
        String result = pointService.usePoint(id, amount, System.currentTimeMillis());

        if ("insufficient".equals(result)) {
            throw new HandlePointException("잔액이 부족합니다.");
        }

        UserPoint updatedUserPoint = pointService.selectPointById(id);
        return new UserPoint(id, updatedUserPoint.point(), System.currentTimeMillis());
    }
}
