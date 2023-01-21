package com.jpabook.www.exception;

public class NotEnoughStockException extends RuntimeException {

    public NotEnoughStockException() {
        super();
    }

    public NotEnoughStockException(String message) { // 메세지
        super(message);
    }

    public NotEnoughStockException(String message, Throwable cause) { // 메세지 + 근원적인 이유
        super(message, cause);
    }

    public NotEnoughStockException(Throwable cause) {
        super(cause);
    }

}
