package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.exception.PointNotEnoughException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@DisplayName("포인트관련 서비스 로직 테스트")
public class PointServiceTest {

    @InjectMocks
    private PointService pointService;

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("ID로 유저를 조회할 때 존재하는 경우를 테스트")
    public void selectUserByIdTest() throws Exception {
        // given : 조회하는 id가 존재할 경우 그 id의 UserPoint를 반환
        long id = 1;
        UserPoint userPoint = new UserPoint(id, 0, System.currentTimeMillis());
        given(userPointTable.selectById(anyLong())).willReturn(userPoint);

        // when : 조회하는 서비스 호출
        UserPoint result = pointService.selectPointById(id);

        // then : 결과값이 id에 셋팅해 준 값과 같은가?
        assertThat(result.id()).isEqualTo(id);
    }

    @Test
    @DisplayName("ID로 유저를 조회할 때 존재하지 않는 경우를 테스트(null)")
    public void selectUserByIdNullTest() {
        // given : 조회하는 id가 존재하지 않을 경우 null을 반환
        long id = 1;
        UserPoint userPoint = new UserPoint(id, 0, System.currentTimeMillis());
        given(userPointTable.selectById(anyLong())).willReturn(null);

        // when : 조회하는 서비스 호출
        UserPoint result = pointService.selectPointById(id);

        // then : 결과값이 null인가? (존재하지 않는가?)
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("포인트 충전 테스트 - 신규 ID")
    public void newChargePointTest() {
        // given : 존재하지 않는 회원일 경우 신규 등록
        long id = 1;
        long amount = 50000;
        UserPoint userPoint = new UserPoint(id, amount, System.currentTimeMillis());

        given(userPointTable.selectById(anyLong())).willReturn(null);
        given(userPointTable.insertOrUpdate(anyLong(), anyLong())).willReturn(userPoint);

        // when : 포인트 충전 서비스 호출
        UserPointResponse result = pointService.chargePoint(id, amount);
        
        // then : 결과값이 예상값과 일치하는지 확인
        assertThat(result.getUserPoint().id()).isEqualTo(id);
        assertThat(result.getUserPoint().point()).isEqualTo(amount);
    }

    @Test
    @DisplayName("포인트 충전 테스트 - 기존 ID에 추가 충전")
    public void chargePointTest() {
        // given : 포인트 추가 충전 (기존 포인트에 + 해줌)
        long id = 1;
        long amount = 50000;
        long addAmount = 30000;
        UserPoint userPoint = new UserPoint(id, amount, 0);

        given(userPointTable.selectById(anyLong())).willReturn(userPoint);  // 존재하는 id 셋팅
        given(userPointTable.insertOrUpdate(anyLong(), anyLong())).willReturn(new UserPoint(id, userPoint.point()+addAmount, 0));

        // when : 포인트 충전 서비스 호출
        UserPointResponse result = pointService.chargePoint(id, addAmount);

        // then : 결과값이 예상값과 일치하는지 확인
        assertThat(result.getUserPoint().id()).isEqualTo(id);
        assertThat(result.getUserPoint().point()).isEqualTo(amount+addAmount);
    }

    @Test
    @DisplayName("포인트 사용 테스트 - 포인트가 부족할시 exception 발생")
    public void usePointNotEnoughTest() {
        // given : 포인트 사용시 기존 포인트 - 사용포인트 값을 반환
        long id = 1;
        long amount = 50000;
        long usePoint = 70000;

        UserPoint userPoint = new UserPoint(id, amount, 0);
        given(userPointTable.selectById(anyLong())).willReturn(userPoint);
        given(userPointTable.insertOrUpdate(anyLong(), anyLong())).willReturn(new UserPoint(userPoint.id(), userPoint.point()-usePoint, 0));

        // when, given : 포인트 사용 서비스를 호출 했을때 PointNotEnoughException을 반환하는가? (남는 포인트가 0보다 작은가?)
        assertThatExceptionOfType(PointNotEnoughException.class)
                .isThrownBy(() -> {
                    pointService.usePoint(id, usePoint);
                }).withMessage("포인트가 부족합니다.");
    }

    @Test
    @DisplayName("포인트 사용 테스트 - 정상적으로 사용 완료")
    public void usePointTest () throws Exception {
        // given : 포인트 사용시 기존 포인트 - 사용포인트 값을 반환
        long id = 1;
        long amount = 50000;
        long usePoint = 30000;

        UserPoint userPoint = new UserPoint(id, amount, 0);
        given(userPointTable.selectById(anyLong())).willReturn(userPoint);
        given(userPointTable.insertOrUpdate(anyLong(), anyLong())).willReturn(new UserPoint(userPoint.id(), userPoint.point()-usePoint, 0));

        // when : 포인트 사용 서비스 호출
        UserPointResponse result = pointService.usePoint(id, usePoint);

        // then : 기존 포인트 - 사용포인트의 값과 일치하는가?
        assertThat(result.getUserPoint().id()).isEqualTo(id);
        assertThat(result.getUserPoint().point()).isEqualTo(amount-usePoint);
    }

    @Test
    @DisplayName("포인트 충전,사용 내역 조회 테스트 - 내역이 없을경우")
    public void selectPointHistorysNullTest () {
        // given : 포인트 내역이 없는 경우를 반환 (size == 0)
        long id = 1;
        List<PointHistory> historyList = new ArrayList<PointHistory>();
        given(pointHistoryTable.selectAllByUserId(anyLong())).willReturn(historyList.stream().toList());

        // when : 포인트 내역 조회 서비스 호출
        List<PointHistory> result = pointService.findAllHistoryById(id);

        // then : 포인트 내역 사이즈가 0인가?
        assertThat(result.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("포인트 충전,사용 내역 조회 테스트")
    public void selectPointHistorysTest () {
        // given : 포인트 내역 생성 및 해당 리스트 조회
        long userId = 1;
        List<PointHistory> phList = List.of(new PointHistory(1, userId, 1000, TransactionType.CHARGE, 0),
                new PointHistory(2, userId, 2000, TransactionType.CHARGE, 0),
                new PointHistory(4, userId, 4000, TransactionType.CHARGE, 0),
                new PointHistory(3, userId, 3000, TransactionType.CHARGE, 0)
        );
        given(pointHistoryTable.selectAllByUserId(anyLong())).willReturn(phList.stream().toList());

        // when : 포인트 내역 조회 서비스 호출
        List<PointHistory> result = pointService.findAllHistoryById(userId);

        // then : 결과값이 예상값과 일치하는가?
        assertThat(result.size()).isEqualTo(phList.size());
        assertThat(result).isEqualTo(phList);
    }

    @Test
    @DisplayName("포인트 충전시 포인트 내역 등록 테스트")
    public void insertPointChargeHistoryTest () {
        // given : 기존 포인트 + 충전 했다고 가정, 내역 저장
        long id = 1;
        long amount = 50000;
        long addPoint = 20000;
        given(userPointTable.insertOrUpdate(anyLong(), anyLong())).willReturn(new UserPoint(id, amount+addPoint, 0));   //
        given(pointHistoryTable.insert(anyLong(),anyLong(),any(),anyLong())).willReturn(new PointHistory(1, id, amount+addPoint, TransactionType.CHARGE, 0));

        // when : 포인트 충전 서비스 호출
        UserPointResponse result = pointService.chargePoint(id, amount);

        // then : 히스토리에 저장된 값이 충전될때 정보랑 일치하는지?
        assertThat(result.getPointHistory().userId()).isEqualTo(id);
        assertThat(result.getPointHistory().amount()).isEqualTo(amount+addPoint);
        assertThat(result.getPointHistory().type()).isEqualTo(TransactionType.CHARGE);
    }

    @Test
    @DisplayName("포인트 사용시 포인트 내역 등록 테스트")
    public void insertPointUseHistoryTest () throws Exception {
        // given : 포인트 사용시 기존 포인트 - 사용포인트 값을 반환
        long id = 1;
        long amount = 50000;
        long usePoint = 20000;

        UserPoint userPoint = new UserPoint(id, amount, 0);
        given(userPointTable.selectById(anyLong())).willReturn(userPoint);
        given(userPointTable.insertOrUpdate(anyLong(), anyLong())).willReturn(new UserPoint(userPoint.id(), userPoint.point()-usePoint, 0));
        given(pointHistoryTable.insert(anyLong(),anyLong(),any(),anyLong())).willReturn(new PointHistory(1, userPoint.id(), userPoint.point()-usePoint, TransactionType.USE, 0));

        // when : 포인트 사용 서비스 호출
        UserPointResponse result = pointService.usePoint(id, usePoint);

        // then : 히스토리에 저장된 값이 사용될때 정보랑 일치하는지?
        assertThat(result.getPointHistory().userId()).isEqualTo(id);
        assertThat(result.getPointHistory().amount()).isEqualTo(amount-usePoint);
        assertThat(result.getPointHistory().type()).isEqualTo(TransactionType.USE);
    }

}
