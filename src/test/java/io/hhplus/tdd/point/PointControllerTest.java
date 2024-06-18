package io.hhplus.tdd.point;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

@SpringBootTest
@AutoConfigureMockMvc
public class PointControllerTest {

    // 웹 API 테스트할 때 사용
    @Autowired
    MockMvc mvc;

    @Test
    @DisplayName("point 충전")
    void chargeTest() throws Exception {
        long id = 1;
        long amount = 50000;

        // 충전 api 호출 테스트를 위해 작성
        mvc.perform(patch("/charge")
                .param("id", String.valueOf(id))
                .param("amount", String.valueOf(amount))
            );
    }
}
