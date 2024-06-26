package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest     // SpringBootTest는 최소 통합테스트 일때만 사용(단위테스트에선 x)
public class PointServiceIntegrationTest {

    @Autowired
    private PointService pointService;
    @Autowired
    private UserPointTable userPointTable;
    @Autowired
    private PointHistoryTable pointHistoryTable;

    @BeforeEach
    void setUp() {
        pointService = new PointService(userPointTable,pointHistoryTable);
    }

    // 동시성 제어
    @Test
    @DisplayName("여러 스레드가 포인트 충전 호출시 동시성 제어 테스트")
    public void chargePointIntegrationTest() throws InterruptedException {
        // given
        int threadCnt = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCnt);  // 고정된 스레드 풀을 생성하여 여러 스레드에서 작업을 실행
        CountDownLatch latch = new CountDownLatch(threadCnt);                       // 모든 스레드가 작업 완료할때 까지 대기

        AtomicLong sucessPoint = new AtomicLong();     // 성공한 결과값
        AtomicLong failCount = new AtomicLong();       // 실패한 수

        for (int i=1; i<=threadCnt; i++) {
            executorService.submit(() -> {      // 각 thread가 chargePoint를 호출
                try {
                   long id = 1;
                   long addPoint = 10000;
                   UserPointResponse userPoint = pointService.chargePoint(id, addPoint);        // 10000p씩 충전
                   sucessPoint.set(userPoint.getUserPoint().point());                           // 충전후 결과값
                } catch (Exception e) {
                    failCount.getAndIncrement();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();                  // 모든 thread가 종료될대까지 기다림
        executorService.shutdown();     // thread풀 종료

        // then : 10000p씩 10번 반복했으므로 100000이 맞게 나오는지 테스트
        assertThat(sucessPoint.get()).isEqualTo(100000);
    }

    // 동시성 제어
    @Test
    @DisplayName("여러 스레드가 포인트 사용 호출시 동시성 제어 테스트")
    public void usePointIntegrationTest() throws InterruptedException {
        // given
        int threadCnt = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCnt); // 고정된 스레드 풀을 생성하여 여러 스레드에서 작업을 실행
        CountDownLatch latch = new CountDownLatch(threadCnt);                       // 모든 스레드가 작업 완료할때 까지 대기

        AtomicLong sucessPoint = new AtomicLong();      // 성공한 결과값
        AtomicLong failCount = new AtomicLong();        // 실패한 수

        UserPointResponse chargePoint = pointService.chargePoint(1, 100000);                // 미리 100000p 충전해놓음

        // when
        for (int i=1; i<=threadCnt; i++) {
            executorService.submit(() -> {
                try {
                    long id = 1;
                    long usePoint = 5000;
                    UserPointResponse userPoint = pointService.usePoint(id, usePoint);        // 5000p 씩 사용
                    sucessPoint.set(userPoint.getUserPoint().point());                          // 사용후 결과값 세팅
                } catch (Exception e) {
                    failCount.getAndIncrement();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();                          // 모든 thread가 종료될대까지 기다림
        executorService.shutdown();             // thread 풀 종료

        // then : 5000p씩 10번 반복했으므로 50000이 맞게 나오는지 테스트
        assertThat(sucessPoint.get()).isEqualTo(50000);
    }

}
