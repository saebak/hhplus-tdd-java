package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.exception.PointNotEnoughException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@AllArgsConstructor
public class PointService {

    private UserPointTable userPointTable;
    private PointHistoryTable pointHistoryTable;
    private final Lock lock = new ReentrantLock();      // 동시성 제어를 위해 선언

    // 포인트 조회
    public UserPoint selectPointById(long id) {
        return userPointTable.selectById(id);
    }

    // 포인트 충전
    public UserPointResponse chargePoint(long id, long amount) {
        UserPointResponse response = new UserPointResponse();
        try {
            lock.lock();        // 동시성 제어
            UserPoint user = userPointTable.selectById(id);
            PointHistory history;

            if (user == null) {
                user = userPointTable.insertOrUpdate(id, amount);
            } else {
                user = userPointTable.insertOrUpdate(id, user.point()+amount);
            }
            response.setUserPoint(user);

            history = pointHistoryTable.insert(user.id(),amount,TransactionType.CHARGE,0);
            response.setPointHistory(history);
        } finally {
            lock.unlock();
        }
        return response;
    }

    // 포인트 사용
    public UserPointResponse usingPoint(long id, long usePoint) throws Exception {
        UserPointResponse response = new UserPointResponse();
        try {
            lock.lock();    // 동시성 제어
            UserPoint user = userPointTable.selectById(id);
            PointHistory history;

            if (user == null) {
                throw new NullPointerException("존재하지 않는 id 입니다.");
            }

            if (user.point() - usePoint < 0) {
                throw new PointNotEnoughException("포인트가 부족합니다.");
            }
            user = userPointTable.insertOrUpdate(id, user.point()-usePoint);

            response.setUserPoint(user);
            history = pointHistoryTable.insert(user.id(),usePoint,TransactionType.USE,0);
            response.setPointHistory(history);
        } finally {
            lock.unlock();
        }
        return response;
    }
    
    // 포인트 내역 조회
    public List<PointHistory> findAllHistoryById(long id) {
        return pointHistoryTable.selectAllByUserId(id);
    }
}
