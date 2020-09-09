package org.codinjutsu.tools.jenkins.view;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

/**
 * @Package: org.codinjutsu.tools.jenkins.view
 * @ClassName: JenkinsTreeRendererTest
 * @Description:
 * @Author: zoutairan
 * @CreateDate: 9/9/2020 10:44 AM
 * @UpdateUser: zoutairan
 * @UpdateDate: 9/9/2020 10:44 AM
 * @UpdateRemark:
 * @Version: 1.0
 * Copyright: Copyright (c) 2020
 */
public class JenkinsTreeRendererTest extends TestCase {
    @Test
    public void testOptional() {
        List<Integer> integerList = null;
        Optional<List<Integer>> optional = Optional.ofNullable(integerList);
        String result = optional.flatMap(integers -> integers.stream()
                .map(Object::toString)
                .reduce((a, b) -> a + b)).orElse("null")
        ;
    }

}