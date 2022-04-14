/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package steganograf.Exceptions;

/**
 *
 * @author ACER
 */
public class CannotEncodeException extends Exception{
    public CannotEncodeException(String message) {super(message);}
    public CannotEncodeException(String message, Throwable cause) {super(message, cause);}
}
