package io.hhplus.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.model.PointValidation;
import io.hhplus.tdd.point.model.TransactionType;
import io.hhplus.tdd.point.model.UserPoint;
import io.hhplus.tdd.point.service.impl.PointServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PointServiceTest {

    private PointServiceImpl pointService;
    private UserPointTable userPointTable;
    private PointHistoryTable pointHistoryTable;

    @BeforeEach
    void setUp() {
        userPointTable = mock(UserPointTable.class);
        pointHistoryTable = mock(PointHistoryTable.class);
        pointService = new PointServiceImpl(userPointTable, pointHistoryTable); // Constructor injection for mocks
    }

    /**
     * 충전 포인트가 유효하지 않은 값(음수)인 경우
     * 충전할 수 없으며, INVALID_AMOUNT를 결과 값으로 취한다.
     */
    @Test
    @DisplayName("유효하지 않은 값 충전 test")
    void testChargePointInvalid() {
        long userId = 3445L;
        long now = System.currentTimeMillis(); // 현재 시간 통일

        // 3445L이라는 ID가 500L 포인트를 갖도록 객체 생성
        UserPoint userPoint = new UserPoint(userId, 500L, now);
        when(userPointTable.selectById(userId)).thenReturn(userPoint);

        // 충전 포인트로 음수로 지정하는 객체 생성
        String result = pointService.chargePoint(userId, -100L, now);

        // INVALID_AMOUNT 음수 값으로 충전 할 수 없음
        assertEquals(PointValidation.INVALID_AMOUNT, result);
    }

    /**
     * 충전 포인트로 인해 최대 포인트 값을 초과 하는 경우
     * 충전할 수 없으며, EXCEED를 결과 값으로 취한다.
     */
    @Test
    @DisplayName("포인트 충전 시 최대 한도 초과 test")
    void testChargePointExceedMax() {
        long userId = 3445L;
        long now = System.currentTimeMillis(); // 현재 시간 통일

        // 3445L이라는 ID가 10_000_000L 인 최대 포인트를 갖도록 객체 생성
        UserPoint userPoint = new UserPoint(userId, 10_000_000L, now);
        when(userPointTable.selectById(userId)).thenReturn(userPoint);

        // 최대 포인트를 이미 도달했지만, 1L 포인트를 충전 하고자 시도
        String result = pointService.chargePoint(userId, 1L, now);
        
        // EXCEED 포인트 초과로 충전 할 수 없음
        assertEquals(PointValidation.EXCEED, result);
    }

    /**
     * 충전 포인트가 음수 이거나, 최대 포인트 값을 초과 하는 경우가 아닌 경우
     * 충전할 수 있으며, SUCCESS를 결과 값으로 취한다.
     */
    @Test
    @DisplayName("포인트 충전 성공 test")
    void testChargePointSuccess() {
        long userId = 3445L;
        long now = System.currentTimeMillis(); // 현재 시간 통일

        // 3445L이라는 ID가 5000L 포인트를 갖도록 객체 생성
        UserPoint userPoint = new UserPoint(userId, 5000L, now);
        when(userPointTable.selectById(userId)).thenReturn(userPoint);

        // 1000L 충전
        String result = pointService.chargePoint(userId, 1000L, now);

        // 충전 포인트(amount) 가 음수도 아니고, 충전 후 포인트가 최대 포인트에 도달하지 않기 때문에 성공
        assertEquals(PointValidation.SUCCESS, result);

        // ArgumentCaptor를 사용하여 시간 값 캡처 (Argument Mismatch 에러 방지)
        ArgumentCaptor<Long> timeCaptor = ArgumentCaptor.forClass(Long.class);

        // insert 된 값이 의도한 값과 동일한지 확인
        verify(userPointTable).insertOrUpdate(eq(userId), eq(6000L));
        verify(pointHistoryTable).insert(eq(userId), eq(6000L), eq(TransactionType.CHARGE), timeCaptor.capture());

    }

    /**
     * 사용 포인트가 음수인 경우,
     * 포인트를 사용할 수 없으며, INSUFFICIENT를 결과 값으로 취한다.
     */
    @Test
    @DisplayName("유효하지 않은 포인트 사용 test")
    void testUsePointInvalid() {
        long userId = 3445L;
        long now = System.currentTimeMillis(); // 현재 시간 통일

        // 3445L이라는 ID가 1000L 포인트를 갖도록 객체 생성
        UserPoint userPoint = new UserPoint(userId, 1000L, now);
        when(userPointTable.selectById(userId)).thenReturn(userPoint);

        // -2000L 포인트 사용 > 음수로 사용할 수 없음
        String result = pointService.usePoint(userId, -2000L, now);
        assertEquals(PointValidation.INVALID_AMOUNT, result);
    }

    /**
     * 사용 포인트가 보유한 포인트 보다 많은 경우,
     * 포인트를 사용할 수 없으며, INSUFFICIENT를 결과 값으로 취한다.
     */
    @Test
    @DisplayName("사용 포인트 부족 test")
    void testUsePointInsufficient() {
        long userId = 3445L;
        long now = System.currentTimeMillis(); // 현재 시간 통일

        // 3445L이라는 ID가 1000L 포인트를 갖도록 객체 생성
        UserPoint userPoint = new UserPoint(userId, 1000L, now);
        when(userPointTable.selectById(userId)).thenReturn(userPoint);

        // 2000L 포인트 사용 > 3445L user 가 가지고있는 포인트 보다 많은 포인트를 사용하고자 했기 때문에 사용할 수 없음
        String result = pointService.usePoint(userId, 2000L, now);
        assertEquals(PointValidation.INSUFFICIENT, result);
    }

    /**
     * 충전 포인트가 음수 이거나, 보유한 포인트 내에서 사용이 가능한 경우
     * 포인트를 사용할 수 있으며, SUCCESS를 결과 값으로 취한다.
     */
    @Test
    @DisplayName("포인트 사용 성공 test")
    void testUsePointSuccess() {
        long userId = 3445L;
        long now = System.currentTimeMillis(); // 현재 시간 통일

        // 3445L이라는 ID가 3000L 포인트를 갖도록 객체 생성
        UserPoint userPoint = new UserPoint(userId, 3000L, now);
        when(userPointTable.selectById(userId)).thenReturn(userPoint);

        // 1000L 포인트 사용
        String result = pointService.usePoint(userId, 2000L, now);
        assertEquals(PointValidation.SUCCESS, result);

        // ArgumentCaptor를 사용하여 시간 값 캡처 (Argument Mismatch 에러 방지)
        ArgumentCaptor<Long> timeCaptor = ArgumentCaptor.forClass(Long.class);

        // 예상 결과 값 확인
        verify(userPointTable).insertOrUpdate(eq(userId), eq(1000L));
        verify(pointHistoryTable).insert(eq(userId), eq(1000L), eq(TransactionType.USE), timeCaptor.capture());
    }

    /**
     * 충전 포인트가 음수 이거나, 최대 포인트 값을 초과 하는 경우가 아닌 경우
     * 충전할 수 있으며, SUCCESS를 결과 값으로 취한다.
     */
    @Test
    @DisplayName("포인트 충전 및 사용에 대한 동시성 test")
    void testConcurrentChargeAndUse() throws InterruptedException {
        long userId = 3445L;
        long now = System.currentTimeMillis(); // 현재 시간 통일

        // 3445L이라는 ID가 10000L 포인트를 갖도록 객체 생성
        UserPoint userPoint = new UserPoint(userId, 10000L, now);
        when(userPointTable.selectById(userId)).thenReturn(userPoint);

        // 10개의 스레드를 생성하여 동시에 포인트를 충전 및 사용하도록 설정
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 5; i++) {
            // 충전 스레드
            executorService.submit(() -> {
                pointService.chargePoint(userId, 1000L, now);
            });

            // 사용 스레드
            executorService.submit(() -> {
                pointService.usePoint(userId, 500L, now);
            });
        }

        // 스레드 완료 대기
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.MINUTES);

        verify(pointHistoryTable, atLeast(5)).insert(eq(userId), anyLong(), eq(TransactionType.CHARGE), anyLong());
        verify(pointHistoryTable, atLeast(5)).insert(eq(userId), anyLong(), eq(TransactionType.USE), anyLong());
    }
}
