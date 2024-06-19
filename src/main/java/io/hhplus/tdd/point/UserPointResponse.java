package io.hhplus.tdd.point;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPointResponse {
    UserPoint userPoint;
    PointHistory pointHistory;
}