package framework.util;

import java.nio.ByteBuffer;

/**
 * The following class is used for converting primitive data types to byte arrays
 * This is important to maintain correct byte array sizes for these data types
 * 
 * @author Andre Allan Ponce
 *
 */
public class ByteConverter {

	public static byte[] charToByte(char num){
		charBuffer.putChar(0,num);
		return charBuffer.array();
	}
	
	public static byte[] intToByte(int num){
		intBuffer.putInt(0,num);
		return intBuffer.array();
	}

	public static byte[] longToByte(long num){
		longBuffer.putLong(0,num);
		return longBuffer.array();
	}

	private static ByteBuffer longBuffer = ByteBuffer.allocate(Long.BYTES);
	private static ByteBuffer charBuffer = ByteBuffer.allocate(Character.BYTES);
	private static ByteBuffer intBuffer = ByteBuffer.allocate(Integer.BYTES);
}

/* PLANTUML CODE
 * 
 * @startuml
 * !define ByteConverterUML
 * 
 * class ByteConverter{
 * 	{static} -longBuffer : ByteBuffer
 * 	{static} -charBuffer : ByteBuffer
 * 	{static} -intBuffer : ByteBuffer
 * 
 * 	+charToByte(char) : byte[]
 * 	+intToByte(int)	: byte[]
 * 	+longToByte(long) : byte[]
 * }
 * 
 * @enduml
 */
