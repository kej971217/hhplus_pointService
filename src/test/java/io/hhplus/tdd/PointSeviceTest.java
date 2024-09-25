package io.hhplus.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.model.TransactionType;
import io.hhplus.tdd.point.model.UserPoint;
import io.hhplus.tdd.point.service.impl.PointServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

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

    @Test
    void testChargePointExceedMax() {
        long userId = 3445L;
        long now = System.currentTimeMillis();
        UserPoint userPoint = new UserPoint(userId, 10_000_000L, now);
        when(userPointTable.selectById(userId)).thenReturn(userPoint);

        String result = pointService.chargePoint(userId, 1L, now);
        assertEquals("exceed", result);
    }

    @Test
    void testChargePointSuccess() {
        long userId = 3445L;
        long now = System.currentTimeMillis();
        UserPoint userPoint = new UserPoint(userId, 5000L, now);
        when(userPointTable.selectById(userId)).thenReturn(userPoint);

        String result = pointService.chargePoint(userId, 1000L, now);
        assertEquals("success", result);

        ArgumentCaptor<Long> timeCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userPointTable).insertOrUpdate(eq(userId), eq(6000L));
        verify(pointHistoryTable).insert(eq(userId), eq(6000L), eq(TransactionType.CHARGE), timeCaptor.capture());

        // 시간 차이가 100ms 이하인지 확인
        assertTrue(Math.abs(timeCaptor.getValue() - now) < 100);
    }

    @Test
    void testUsePointInsufficient() {
        Long userId = 3445L;
        long now = System.currentTimeMillis();
        UserPoint userPoint = new UserPoint(userId, 1000L, now);
        when(userPointTable.selectById(userId)).thenReturn(userPoint);

        String result = pointService.usePoint(userId, 2000L, now);
        assertEquals("insufficient", result);
    }

    @Test
    void testUsePointSuccess() {
        Long userId = 3445L;
        long now = System.currentTimeMillis();
        UserPoint userPoint = new UserPoint(userId, 2000L, now);
        when(userPointTable.selectById(userId)).thenReturn(userPoint);

        String result = pointService.usePoint(userId, 1000L, now);
        assertEquals("success", result);

        // ArgumentCaptor로 시간을 캡처
        ArgumentCaptor<Long> timeCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userPointTable).insertOrUpdate(eq(userId), eq(1000L));
        verify(pointHistoryTable).insert(eq(userId), eq(1000L), eq(TransactionType.USE), timeCaptor.capture());

        // 캡처된 시간과 현재 시간이 100ms 이하의 차이인지 확인
        assertTrue(Math.abs(timeCaptor.getValue() - now) < 100);
    }
}
