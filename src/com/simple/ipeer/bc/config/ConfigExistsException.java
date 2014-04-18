/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.simple.ipeer.bc.config;

/**
 *
 * @author iPeer
 */
public class ConfigExistsException extends Exception {

    /**
     * Creates a new instance of <code>ConfigExistsException</code> without detail message.
     */
    public ConfigExistsException() {
    }

    /**
     * Constructs an instance of <code>ConfigExistsException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public ConfigExistsException(String msg) {
	super(msg);
    }
}
