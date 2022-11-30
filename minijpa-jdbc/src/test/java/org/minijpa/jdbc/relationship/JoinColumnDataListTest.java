package org.minijpa.jdbc.relationship;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JoinColumnDataListTest {
    @Test
    public void joinColumns() {
        JoinColumnData joinColumnData1 = new JoinColumnData(Optional.of("code_id"), Optional.of("cid"));
        JoinColumnData joinColumnData2 = new JoinColumnData(Optional.of("user_id"), Optional.of("uid"));
        JoinColumnDataList joinColumnDataList = new JoinColumnDataList(Arrays.asList(joinColumnData1, joinColumnData2));
        Optional<String> optional = joinColumnDataList.getNameByReferenced("uid");
        Assertions.assertTrue(optional.isPresent());
        Assertions.assertEquals("user_id", optional.get());
    }
}
