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
public class CannotDecodeException extends Exception{
    public CannotDecodeException(String message) {super(message);}
    public CannotDecodeException(String message, Throwable cause) {super(message, cause);}
}
