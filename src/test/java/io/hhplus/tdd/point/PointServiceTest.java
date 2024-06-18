package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@SpringBootTest
public class PointServiceTest {

    @InjectMocks
    private PointService pointService;

    @Mock
    private UserPointTable userPointTable;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pointService = new PointService(userPointTable);
    }

    @Test
    public void 유저_조회_존재하지않음() throws Exception {

        // given
        long id = 1;
        UserPoint userPoint = new UserPoint(id, 0, System.currentTimeMillis());
        given(userPointTable.selectById(anyLong())).willReturn(userPoint);

        // when
        UserPoint result = userPointTable.selectById(id);

        // then
        assertThat(result.id()).isEqualTo(id);
    }

    @Test
    public void 유저_조회_존재() {

        // given
        long id = 1;
        UserPoint userPoint = new UserPoint(id, 0, System.currentTimeMillis());
        given(userPointTable.selectById(anyLong())).willReturn(null);

        // when
        UserPoint result = userPointTable.selectById(id);

        // then
        assertThat(result).isNull();
    }

    @Test
    public void 포인트_충전_신규() {
        // given
        long id = 1;
        long amount = 50000;
        UserPoint userPoint = new UserPoint(id, amount, System.currentTimeMillis());

        given(userPointTable.selectById(anyLong())).willReturn(null);   // 존재하지 않는 id일 경우 조회했을때 null return
        given(userPointTable.insertOrUpdate(anyLong(), anyLong())).willReturn(userPoint);   // 존재하지 않는 회원일 경우 신규 등록

        // when
        UserPoint result = pointService.chargePoint(id, amount);            // 포인트 충전 서비스 호출
        
        // then()
        assertThat(result.id()).isEqualTo(userPoint.id());
        assertThat(result.point()).isEqualTo(userPoint.point());
    }

    @Test
    public void 포인트_충전_추가() {

        // given
        long id = 1;
        long amount = 50000;
        long addAmount = 30000;
        UserPoint userPoint = new UserPoint(id, amount, 0);

        given(userPointTable.selectById(anyLong())).willReturn(userPoint);  // 존재하는 id 셋팅
        given(userPointTable.insertOrUpdate(anyLong(), anyLong())).willReturn(new UserPoint(id, amount+addAmount, 0));  // 포인트 추가 충전

        // when
        UserPoint result = pointService.chargePoint(id, addAmount);

        // then
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.point()).isEqualTo(amount+addAmount);
    }
}
