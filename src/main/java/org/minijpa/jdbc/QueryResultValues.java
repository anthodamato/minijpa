/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.minijpa.jdbc;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author adamato
 */
public class QueryResultValues {

    public List<Object> values = new ArrayList<>();
    public List<MetaAttribute> attributes = new ArrayList<>();
    public List<Object> relationshipValues = new ArrayList<>();
    public List<MetaAttribute> relationshipAttributes = new ArrayList<>();

}
