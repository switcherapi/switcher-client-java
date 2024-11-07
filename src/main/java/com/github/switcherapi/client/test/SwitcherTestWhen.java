package com.github.switcherapi.client.test;

import com.github.switcherapi.client.model.StrategyValidator;

public @interface SwitcherTestWhen {

    StrategyValidator strategy();

    String[] input();

}
