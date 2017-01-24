package org.wso2.siddhi.extension.var.models;

import java.util.Map;

/**
 * Created by dilini92 on 1/9/17.
 */
public class ParametricPortfolio extends Portfolio {
    public ParametricPortfolio(String ID, Map<String, Integer> assets){
        super(ID, assets);
    }
}
