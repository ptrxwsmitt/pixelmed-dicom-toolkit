package com.pixelmed.convert;

import java.io.EOFException;
import java.io.IOException;
import java.io.RandomAccessFile;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TIFFFile {

	protected RandomAccessFile raf;
	protected ByteOrder byteOrder;
	protected int version;
	protected boolean isBigTIFF;
	protected int byteSizeOfOffsets;
	protected String filename;

	private final byte[] buffer16 = new byte[2];
	private final byte[] buffer32 = new byte[4];
	private final byte[] buffer64 = new byte[8];
	
	public void seek(long pos) throws IOException {
		raf.seek(pos);
	}
	
	public long getFilePointer() throws IOException {
		return raf.getFilePointer();
	}
	
	public int read(byte[] b) throws IOException {
		return raf.read(b);
	}
	
	public int read(byte[] b,int off,int len) throws IOException {
		return raf.read(b,off,len);
	}

	public int read(short[] s) throws IOException {
		byte[] b =  new byte[s.length*2];
		int readBytes = raf.read(b);
		ByteBuffer.wrap(b).order(byteOrder).asShortBuffer().get(s);
		return readBytes/2;
	}
	
	public int read(short[] s,int off,int len) throws IOException {
		byte[] b =  new byte[len*2];
		int readBytes = raf.read(b);
		ByteBuffer.wrap(b).order(byteOrder).asShortBuffer().get(s,off,len);
		return readBytes/2;
	}

	public int read(float[] f) throws IOException {
		byte[] b =  new byte[f.length*4];
		int readBytes = raf.read(b);
		ByteBuffer.wrap(b).order(byteOrder).asFloatBuffer().get(f);
		return readBytes/4;
	}
	
	public int read(float[] f,int off,int len) throws IOException {
		byte[] b =  new byte[len*4];
		int readBytes = raf.read(b);
		ByteBuffer.wrap(b).order(byteOrder).asFloatBuffer().get(f,off,len);
		return readBytes/4;
	}

	public final int getUnsigned16() throws EOFException, IOException, TIFFException {
		raf.read(buffer16);
		return ((int)ByteBuffer.wrap(buffer16).order(byteOrder).getShort()) & 0x0000FFFF;			// always unsigned
	}
	
	public final long getUnsigned32() throws EOFException, IOException, TIFFException {
		raf.read(buffer32);
		return ((long)ByteBuffer.wrap(buffer32).order(byteOrder).getInt()) & 0x00000000FFFFFFFFl;		// always unsigned
	}
	
	public final long getUnsigned64() throws EOFException, IOException, TIFFException {
		raf.read(buffer64);
		return ByteBuffer.wrap(buffer64).order(byteOrder).getLong();
	}
		
	public final long getOffset() throws EOFException, IOException, TIFFException {
		long offset = 0;
		if (byteSizeOfOffsets == 4) {
			offset = getUnsigned32();
		}
		else if (byteSizeOfOffsets == 8) {
			offset = getUnsigned64();
		}
		else {
			throw new TIFFException("Unsupported byte size of offset "+byteSizeOfOffsets);
		}
		
		return offset;
	}
	
	public ByteOrder getByteOrder() {
		return byteOrder;
	}
	
	public boolean isBigTIFF() {
		return isBigTIFF;
	}

	public TIFFFile(String filename) throws IOException, TIFFException {
		this.filename = filename;
		raf = new RandomAccessFile(filename,"r");
		
		byteOrder = null;
		raf.read(buffer16);
//System.err.println("byte order indicator[0]="+(buffer16[0]&0xff));
//System.err.println("byte order indicator[1]="+(buffer16[1]&0xff));
		if (buffer16[0] == 'I' && buffer16[1] == 'I') {
			byteOrder = ByteOrder.LITTLE_ENDIAN;
		}
		else if (buffer16[0] == 'M' && buffer16[1] == 'M') {
			byteOrder = ByteOrder.BIG_ENDIAN;
		}
		else {
			throw new TIFFException("Not a TIFF or BigTIFF file - byte order not II or MM");
		}

		version = getUnsigned16();
//System.err.println("version="+version);

		if (version == 42) {
			isBigTIFF = false;
		}
		else if (version == 43) {
			isBigTIFF = true;
		}
		else {
			throw new TIFFException("Not a TIFF or BigTIFF file - version is not 42 or 43");
		}
		
		byteSizeOfOffsets = 4;
		if (isBigTIFF) {
			byteSizeOfOffsets = getUnsigned16();
			int reserved = getUnsigned16();
			if (reserved != 0) {
				throw new TIFFException("Reserved field after byte size of offsets is not zero");
			}
		}
		if (byteSizeOfOffsets != 4 && byteSizeOfOffsets != 8) {
			throw new TIFFException("Byte size of offsets of other than 4 or 8 not supported");
		}
//System.err.println("byteSizeOfOffsets="+byteSizeOfOffsets);
	}
	
	public String toString() {
		// as per libtiff tiffdump tool
		StringBuffer buf = new StringBuffer();
		buf.append("Magic: "+(byteOrder == ByteOrder.LITTLE_ENDIAN ? "0x4949 <little-endian>" : "0x4d4d <big-endian>")+" Version: 0x"+Integer.toHexString(version)+(isBigTIFF ? " <BigTIFF>" : "")+"\n");
		buf.append("OffsetSize: 0x"+Integer.toHexString(byteSizeOfOffsets)+" Unused: 0\n");
		return buf.toString();
	}
	
	public String getFileName() {
		return filename;
	}

}

