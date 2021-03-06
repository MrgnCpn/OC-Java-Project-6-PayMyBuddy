package com.paymybuddy.paymybuddyweb.units.utils;

import com.paymybuddy.paymybuddyweb.utils.MSStringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MSStringUtilsTest {

    @Tag("MSStringUtilsTest")
    @Test
    void isEmpty_test() {
        assertThat(MSStringUtils.isEmpty(null)).isEqualTo(true);
        assertThat(MSStringUtils.isEmpty("")).isEqualTo(true);
        assertThat(MSStringUtils.isEmpty(" ")).isEqualTo(false);
        assertThat(MSStringUtils.isEmpty("a")).isEqualTo(false);
    }

    @Tag("MSStringUtilsTest")
    @Test
    void firstUpperCase_test() {
        assertThat(MSStringUtils.firstUpperCase("string")).isEqualTo("String");
    }
}