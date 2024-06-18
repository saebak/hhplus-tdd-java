package io.hhplus.tdd.exception;

import java.io.IOException;

public class PointNotEnoughException extends IOException {
    public PointNotEnoughException(String msg) {
        super(msg);
    }
}
