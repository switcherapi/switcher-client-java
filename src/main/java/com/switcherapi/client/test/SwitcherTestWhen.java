package com.switcherapi.client.test;

import com.switcherapi.client.model.StrategyValidator;

public @interface SwitcherTestWhen {

    StrategyValidator strategy();

    String[] input();

}
