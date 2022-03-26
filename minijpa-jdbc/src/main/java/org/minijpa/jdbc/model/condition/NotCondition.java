/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jdbc.model.condition;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class NotCondition implements Condition {

    private final Condition condition;

    public NotCondition(Condition condition) {
	this.condition = condition;
    }

    @Override
    public ConditionType getConditionType() {
	return ConditionType.NOT;
    }

    public Condition getCondition() {
	return condition;
    }

}
