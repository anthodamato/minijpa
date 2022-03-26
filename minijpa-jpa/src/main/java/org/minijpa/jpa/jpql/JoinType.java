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
public class JoinType {

    private boolean left;
    private boolean outer;
    private boolean inner;

    public boolean isLeft() {
	return left;
    }

    public void setLeft(boolean left) {
	this.left = left;
    }

    public boolean isOuter() {
	return outer;
    }

    public void setOuter(boolean outer) {
	this.outer = outer;
    }

    public boolean isInner() {
	return inner;
    }

    public void setInner(boolean inner) {
	this.inner = inner;
    }

}
