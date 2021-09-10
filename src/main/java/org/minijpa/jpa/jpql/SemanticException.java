/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jpa.jpql;

/**
 *
 * @author Antonio Damato <anto.damato@gmail.com>
 */
public class SemanticException extends RuntimeException {

    public SemanticException(String message) {
	super(message);
    }

}
