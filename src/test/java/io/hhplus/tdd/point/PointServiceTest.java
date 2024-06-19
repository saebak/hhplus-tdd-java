package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.exception.PointNotEnoughException;
import org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

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
    public void 유저_조회_존재() throws Exception {
        // given
        long id = 1;
        UserPoint userPoint = new UserPoint(id, 0, System.currentTimeMillis());
        given(userPointTable.selectById(anyLong())).willReturn(userPoint);  // 조회하는 id가 존재할 경우 그 id의 UserPoint를 반환

        // when
        UserPoint result = pointService.selectPointById(id);                // 조회하는 서비스 호출

        // then
        assertThat(result.id()).isEqualTo(1);                       // 결과값이 id에 셋팅해 준 값과 같은가?
    }

    @Test
    public void 유저_조회_존재하지않음() {
        // given
        long id = 1;
        UserPoint userPoint = new UserPoint(id, 0, System.currentTimeMillis());
        given(userPointTable.selectById(anyLong())).willReturn(null);    // 조회하는 id가 존재하지 않을 경우 null을 반환

        // when
        UserPoint result = pointService.selectPointById(id);                // 조회하는 서비스 호출

        // then
        assertThat(result).isNull();                                        // 결과값이 null인가? (존재하지 않는가?)
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
        UserPointResponse result = pointService.chargePoint(id, amount);            // 포인트 충전 서비스 호출
        
        // then()
        assertThat(result.getUserPoint().id()).isEqualTo(1);                       // 충전하려는 id가 일치하는가?
        assertThat(result.getUserPoint().point()).isEqualTo(50000);                // 처음에 충전하려 했던 포인트와 일치하는가?
    }

    @Test
    public void 포인트_충전_추가() {

        // given
        long id = 1;
        long amount = 50000;
        long addAmount = 30000;
        UserPoint userPoint = new UserPoint(id, amount, 0);

        given(userPointTable.selectById(anyLong())).willReturn(userPoint);  // 존재하는 id 셋팅
        given(userPointTable.insertOrUpdate(anyLong(), anyLong())).willReturn(new UserPoint(id, userPoint.point()+addAmount, 0));  // 포인트 추가 충전 (기존 포인트에 + 해줌)

        // when
        UserPointResponse result = pointService.chargePoint(id, addAmount);     // 포인트 충전 서비스 호출

        // then
        assertThat(result.getUserPoint().id()).isEqualTo(1);                   // 충전하려는 id가 일치하는가?
        assertThat(result.getUserPoint().point()).isEqualTo(80000);            // 기존 포인트 + 충전 포인트의 값과 일치하는가?
    }

    @Test
    public void 포인트_사용_부족() {
        // given
        long id = 1;
        long amount = 50000;
        long usePoint = 70000;

        UserPoint userPoint = new UserPoint(id, amount, 0);
        given(userPointTable.selectById(anyLong())).willReturn(userPoint);
        // 포인트 사용시 기존 포인트 - 사용포인트 값을 반환
        given(userPointTable.insertOrUpdate(anyLong(), anyLong())).willReturn(new UserPoint(userPoint.id(), userPoint.point()-usePoint, 0));

        // 이 부분은 그럼 when 부분이 then 부분과 합쳐진 것인지?
        assertThatExceptionOfType(PointNotEnoughException.class)
                .isThrownBy(() -> {
                pointService.usingPoint(id, usePoint);      // 포인트 사용 서비스를 호출 했을때 PointNotEnoughException을 반환하는가? (남는 포인트가 0보다 작은가?)
            }).withMessage("포인트가 부족합니다.");
    }

    @Test
    public void 포인트_사용_성공 () throws Exception {
        // given
        long id = 1;
        long amount = 50000;
        long usePoint = 30000;

        UserPoint userPoint = new UserPoint(id, amount, 0);
        given(userPointTable.selectById(anyLong())).willReturn(userPoint);
        // 포인트 사용시 기존 포인트 - 사용포인트 값을 반환
        given(userPointTable.insertOrUpdate(anyLong(), anyLong())).willReturn(new UserPoint(userPoint.id(), userPoint.point()-usePoint, 0));

        // when
        UserPointResponse result = pointService.usingPoint(id, usePoint);   // 포인트 사용 서비스 호출

        // then
        assertThat(result.getUserPoint().id()).isEqualTo(1);
        assertThat(result.getUserPoint().point()).isEqualTo(20000);       // 기존 포인트 - 사용포인트의 값과 일치하는가?
    }

    @Test
    public void 포인트_내역_조회_없음 () {
        // given
        long id = 1;
        List<PointHistory> phList = new ArrayList<PointHistory>();
        given(pointHistoryTable.selectAllByUserId(anyLong())).willReturn(phList.stream().toList());     // 포인트 내역이 없는 경우를 반환 (size == 0)

        // when
        List<PointHistory> result = pointService.findAllHistoryById(id);        // 포인트 내역 조회 서비스 호출

        // then
        assertThat(result.size()).isEqualTo(0);                         // 포인트 내역 사이즈가 0인가?
    }

    @Test
    public void 포인트_내역_조회 () {
        // given
        long userId = 1;

        // 포인트 내역 생성
        List<PointHistory> phList = List.of(new PointHistory(1, userId, 1000, TransactionType.CHARGE, 0),
                new PointHistory(2, userId, 2000, TransactionType.CHARGE, 0),
                new PointHistory(3, userId, 3000, TransactionType.CHARGE, 0),
                new PointHistory(4, userId, 4000, TransactionType.CHARGE, 0)
        );
        // 포인트 내역을 반환
        given(pointHistoryTable.selectAllByUserId(anyLong())).willReturn(phList.stream().toList());

        // when
        List<PointHistory> result = pointService.findAllHistoryById(userId);    // 포인트 내역 조회 서비스 호출

        // then
        assertThat(result.size()).isEqualTo(4);                    // size가 예상한 값과 일치하는가?

        // 죄송한데 이거 stream으로 해서 코드 간결하게 짤 수 있을까요.?? 제가 stream이 익숙치가 않아가지고..주륵
        for (int i=0; i<4; i++) {
            assertThat(result.get(i).userId()).isEqualTo(1);           // 포인트 내역 id 가 예상값과 일치하는가?
        }
    }

    @Test
    public void 포인트_내역_충전_등록 () {
        // given
        long id = 1;
        long amount = 50000;
        long addPoint = 20000;

        given(userPointTable.insertOrUpdate(anyLong(), anyLong())).willReturn(new UserPoint(id, amount+addPoint, 0));   // 기존 포인트 + 충전 했다고 가정
        // 충전 히스토리 반환
        given(pointHistoryTable.insert(anyLong(),anyLong(),any(),anyLong())).willReturn(new PointHistory(1, id, amount+addPoint, TransactionType.CHARGE, 0));

        // when
        UserPointResponse result = pointService.chargePoint(id, amount);        // 근데 이부분은 통합테스트 영역이 아닐까요.??? 헷갈리네요

        assertThat(result.getPointHistory().userId()).isEqualTo(1);     // 히스토리에 저장된 값이 충전될때 정보랑 일치하는지?
        assertThat(result.getPointHistory().amount()).isEqualTo(70000);
        assertThat(result.getPointHistory().type()).isEqualTo(TransactionType.CHARGE);
    }

    @Test
    public void 포인트_내역_사용_등록 () throws Exception {
        // given
        long id = 1;
        long amount = 50000;
        long usePoint = 20000;

        UserPoint userPoint = new UserPoint(id, amount, 0);

        given(userPointTable.selectById(anyLong())).willReturn(userPoint);
        // 포인트 사용시 기존 포인트 - 사용포인트 값을 반환
        given(userPointTable.insertOrUpdate(anyLong(), anyLong())).willReturn(new UserPoint(userPoint.id(), userPoint.point()-usePoint, 0));
        // 충전 히스토리 반환
        given(pointHistoryTable.insert(anyLong(),anyLong(),any(),anyLong())).willReturn(new PointHistory(1, userPoint.id(), userPoint.point()-usePoint, TransactionType.USE, 0));

        // when
        UserPointResponse result = pointService.usingPoint(id, usePoint);

        assertThat(result.getPointHistory().userId()).isEqualTo(1);     // 히스토리에 저장된 값이 사용될때 정보랑 일치하는지?
        assertThat(result.getPointHistory().amount()).isEqualTo(30000);
        assertThat(result.getPointHistory().type()).isEqualTo(TransactionType.USE);
    }

}
