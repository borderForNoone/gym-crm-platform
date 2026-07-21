package com.gym.crm.bdd.hooks;

import com.gym.crm.bdd.support.AutomationTestStack;
import io.cucumber.java.AfterAll;
import io.cucumber.java.BeforeAll;

public class TestHooks {
    private static final String STACK_ENABLED = "system.tests.stack.enabled";

    @BeforeAll
    public static void beforeAll() {
        if (isStackEnabled()) {
            AutomationTestStack.start();
        }
    }

    @AfterAll
    public static void afterAll() {
        if (isStackEnabled()) {
            AutomationTestStack.stop();
        }
    }

    private static boolean isStackEnabled() {
        return Boolean.parseBoolean(
                System.getProperty(STACK_ENABLED, "false")
        );
    }
}