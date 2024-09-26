package io.hhplus.tdd.point.model;

public class PointValidation {
    public static final int MAX_POINT = 10_000_000;
    public static final String EXCEED = "exceed";
    public static final String SUCCESS = "success";
    public static final String INSUFFICIENT = "insufficient";
    public static final String INVALID_AMOUNT = "invalid_amount";

    /**
     * param : amount
     * 충전 후 포인트가 최대 포인트를 초과하는지 확인
     */
    public static boolean isValidAmount(Long amount) {
        return amount < 0;
    }

    /**
     * param : userPoint, amount
     * 충전 후 포인트가 최대 포인트를 초과하는지 확인
     */
    public static boolean isExceedingMaxPoint(long userPoint, long amount) {
        return userPoint + amount > MAX_POINT;
    }

    /**
     * param : userPoint, amount
     * 사용 전 포인트가 현재 보유 포인트를 초과하는지 확인
     */
    public static boolean hasInsufficientPoints(UserPoint userPoint, long amount) {
        return userPoint.point() < amount;
    }
}
