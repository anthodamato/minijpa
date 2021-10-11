/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.jpql;

import org.minijpa.jdbc.model.condition.Condition;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public interface ConditionNode {

	public Condition getCondition();

	public void setCondition(Condition condition);
}
