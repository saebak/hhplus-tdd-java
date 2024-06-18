package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PointService {

    private UserPointTable userPointTable;

    // 포인트 조회
    public UserPoint selectPointById(long id) {
        return userPointTable.selectById(id);
    }

    // 포인트 충전
    public UserPoint chargePoint(long id, long amount) {
        UserPoint user = userPointTable.selectById(id);

        if (user == null) {
            user = userPointTable.insertOrUpdate(id, amount);
        } else {
            user = userPointTable.insertOrUpdate(id, user.point()+amount);
        }
        return user;
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
