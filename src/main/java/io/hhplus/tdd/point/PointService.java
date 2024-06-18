package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PointService {

    UserPointTable userPointTable;

    // 포인트 조회
    public long selectPointById(long id) {
        return 0;
    }

    // 포인트 충전
    public UserPoint chargePoint(long id, long amount) {
        UserPoint result = userPointTable.insertOrUpdate(id, amount);
        return result;
    }

    // 포인트 사용
    public UserPoint usingPoint(long id, long amount) {
        return null;
    }
    
    // 포인트 내역 조회
    public PointHistory selectPointHistory(long id) {
        return null;
    }
}
