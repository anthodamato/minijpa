package org.minijpa.jdbc;

/**
 * Same constants as JPA.
 *
 * @author adamato
 *
 */
public enum PkGenerationType {
    TABLE,
    SEQUENCE,
    IDENTITY,
    AUTO,
    PLAIN
}
