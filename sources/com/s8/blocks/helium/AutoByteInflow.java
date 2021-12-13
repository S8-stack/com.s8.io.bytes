package com.s8.blocks.helium;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * 
 * @author pierreconvert
 *
 */
public abstract class AutoByteInflow extends BaseByteInflow {


	protected ByteBuffer buffer;

	/**
	 * 
	 * @param buffer
	 */
	public AutoByteInflow() {
		super();
	}


	public abstract void allocate(int bytecount) throws IOException;


	public abstract void pull() throws IOException;


	@Override
	public boolean isMatching(byte[] sequence) throws IOException {
		int length = sequence.length;
		int offset = 0, remaining;
		while(length > 0) {
			remaining = buffer.remaining();
			// not enough space
			if(remaining < length) {
				// match what can be matched
				for(int i=0; i<remaining; i++) {
					if(sequence[i+offset] != buffer.get()) {
						return false;
					}
				}
				length-=remaining;
				offset+=remaining;
				pull();
			}
			else { // enough space to match remaining bytes
				for(int i=0; i<length; i++) {
					if(sequence[i+offset] != buffer.get()) {
						return false;
					}
				}
				length = 0;
			}
		}
		return true;
	}


	@Override
	public byte getByte() throws IOException {
		allocate(1);
		return buffer.get();
	}


	@Override
	public boolean[] getFlags8() throws IOException {
		allocate(1);
		boolean[] flags = new boolean[8];
		byte b = buffer.get();
		flags[0] = (b & 0x80) == 0x80;
		flags[1] = (b & 0x40) == 0x40;
		flags[2] = (b & 0x20) == 0x20;
		flags[3] = (b & 0x10) == 0x10;
		flags[4] = (b & 0x08) == 0x08;
		flags[5] = (b & 0x04) == 0x04;
		flags[6] = (b & 0x02) == 0x02;
		flags[7] = (b & 0x01) == 0x01;
		return flags;
	}


	@Override
	public byte[] getByteArray(int length) throws IOException {
		byte[] bytes = new byte[length];
		/*
		prepare(length);
		byte[] bytes = new byte[length];
		buffer.get(bytes);

		 */

		// /!\ No block allocation

		int offset = 0, remaining;
		while(length>0) {
			remaining = buffer.remaining();

			// not enough space
			if(remaining < length) {
				buffer.get(bytes, offset, remaining);
				length-=remaining;
				offset+=remaining;
				pull();
			}
			// enough space to write remaining bytes
			else {
				buffer.get(bytes, offset, length);
				length = 0;
			}
		}
		return bytes;
	}
	

	@Override
	public int getUInt8() throws IOException {
		allocate(1);
		return buffer.get() & 0xff;
	}


	@Override
	public int getUInt16() throws IOException {
		allocate(2);
		byte b0 = buffer.get();
		byte b1 = buffer.get();
		return ((b0 & 0xff) << 8 ) | (b1 & 0xff);
	}



	@Override
	public short getInt16() throws IOException {
		allocate(2);
		return buffer.getShort();
	}


	@Override
	public int getInt32() throws IOException {
		allocate(4);
		return buffer.getInt();
	}

	@Override
	public int[] getInt32Array() throws IOException {
		// retrieve length
		allocate(4);
		int length = (int) getUInt32();

		allocate(4*length);
		int[] array = new int[length];
		for(int i=0; i<length; i++) {
			array[i] = buffer.getInt();
		}
		return array;
	}

	
	

	@Override
	public long getInt64() throws IOException {
		allocate(8);
		return buffer.getLong();
	}


	@Override
	public long[] getInt64Array() throws IOException {
		allocate(4);
		// retrieve length
		int length = getUInt31();

		allocate(8*length);
		long[] array = new long[length];
		for(int i=0; i<length; i++) {
			array[i] = buffer.getLong();
		}
		return array;
	}


	@Override
	public int getUInt() throws IOException {
		allocate(1);
		byte b = buffer.get(); // first byte

		if((b & 0x80) == 0x80) {
			int value = b & 0x7f;

			allocate(1);
			b = buffer.get(); // second byte
			if((b & 0x80) == 0x80) {
				value = (value << 7) | (b & 0x7f);

				allocate(1);
				b = buffer.get(); // third byte

				if((b & 0x80) == 0x80) {
					value = (value << 7) | (b & 0x7f);

					allocate(1);
					b = buffer.get(); // fourth byte

					if((b & 0x80) == 0x80) {
						value = (value << 7) | (b & 0x7f);

						allocate(1);
						b = buffer.get(); // fifth byte (final one)

						return (value << 7) | (b & 0x7f);
					}
					else { // fourth byte is matching 0x7f mask
						return (value << 7) | b;
					}
				}
				else { // third byte is matching 0x7f mask
					return (value << 7) | b;
				}
			}
			else { // second byte is matching 0x7f mask
				return (value << 7) | b;
			}
		}
		else { // first byte is matching 0x7f mask
			return b;
		}
	}
	
	
	




	

	@Override
	public float getFloat32() throws IOException {
		allocate(4);
		return buffer.getFloat();
	}


	@Override
	public float[] getFloat32Array() throws IOException {
		allocate(4);
		int length = getUInt31();

		allocate(4*length);
		float[] array = new float[length];
		for(int i=0; i<length; i++) {
			array[i] = buffer.getFloat();
		}
		return array;
	}


	@Override
	public double getFloat64() throws IOException {
		allocate(8);
		return buffer.getDouble();
	}


	@Override
	public double[] getFloat64Array() throws IOException {
		allocate(4);
		int length = getUInt31();

		allocate(8*length);
		double[] array = new double[length];
		for(int i=0; i<length; i++) {
			array[i] = buffer.getDouble();
		}
		return array;
	}


	/**
	 * Max <code>String</code> length is 65536
	 * @return String
	 * @throws IOException 
	 */
	@Override
	public String getL32StringUTF8() throws IOException {

		// read unsigned int
		int bytecount = (int) getUInt32();

		// retrieve all bytes
		byte[] bytes = getByteArray(bytecount);
		return new String(bytes, StandardCharsets.UTF_8);
	}


	@Override
	public String getL8StringASCII() throws IOException {
		// read unsigned int
		int length = getUInt8();

		// retrieve all bytes
		byte[] bytes = getByteArray(length);
		return new String(bytes, StandardCharsets.US_ASCII);
	}


}