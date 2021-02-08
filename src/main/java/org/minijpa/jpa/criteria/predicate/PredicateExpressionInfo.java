/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.criteria.predicate;

import java.util.List;
import javax.persistence.criteria.Expression;

/**
 *
 * @author adamato
 */
public interface PredicateExpressionInfo {

    public List<Expression<?>> getSimpleExpressions();
}
